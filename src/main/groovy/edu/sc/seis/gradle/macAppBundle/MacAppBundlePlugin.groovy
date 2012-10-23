package edu.sc.seis.gradle.macAppBundle;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.CopySpec
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Exec;


class MacAppBundlePlugin implements Plugin<Project> {

    static final String PLUGIN_NAME = "macAppBundle"
    static final String GROUP = PLUGIN_NAME

    static final String TASK_CONFIGURE_NAME = "configMacApp"
    static final String TASK_INFO_PLIST_GENERATE_NAME = "generatePlist"
    static final String TASK_PKG_INFO_GENERATE_NAME = "generatePkgInfo"

    static final String TASK_LIB_COPY_NAME = "copyToResourcesJava"
    static final String TASK_COPY_STUB_NAME = "copyStub"
    static final String TASK_COPY_ICON_NAME = "copyIcon"
    static final String TASK_SET_FILE_NAME = "runSetFile"
    static final String TASK_CREATE_APP_NAME = "createApp"
    static final String TASK_CODE_SIGN_NAME = "codeSign"
    static final String TASK_CREATE_DMG = "createDmg"


    def void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        MacAppBundlePluginExtension pluginExtension = new MacAppBundlePluginExtension()
        project.extensions.macAppBundle = pluginExtension

        Task configTask = addConfigurationTask(project)
        Task plistTask = addCreateInfoPlistTask(project)
        plistTask.dependsOn(configTask)
        Task copyTask = addCopyToLibTask(project)
        copyTask.dependsOn(configTask)
        Task stubTask = addCopyStubTask(project)
        stubTask.dependsOn(configTask)
        Task copyIconTask = addCopyIconTask(project)
        copyIconTask.dependsOn(configTask)
        Task pkgInfoTask = createPkgInfoTask(project)
        pkgInfoTask.dependsOn(configTask)
        Task createAppTask = addCreateAppTask(project)
        createAppTask.dependsOn(plistTask)
        createAppTask.dependsOn(copyTask)
        createAppTask.dependsOn(stubTask)
        createAppTask.dependsOn(copyIconTask)
        createAppTask.dependsOn(pkgInfoTask)
        Task setFileTask = addSetFileTask(project)
        setFileTask.dependsOn(createAppTask)
        /* I think setfile is not required for a .app to be run on osx.
         * Leaving the task in, but not depended on by anything else. If
         * SetFile is needed, then switch the above depends to
         createAppTask.dependsOn(setFileTask)
         */
        Task codeSignTask = addCodeSignTask(project)
        codeSignTask.dependsOn(createAppTask)
        Task dmgTask = addDmgTask(project)
        dmgTask.dependsOn(createAppTask)
        project.getTasksByName("assemble", true).each{ t -> t.dependsOn(dmgTask) }
    }

    private Task addConfigurationTask(Project project) {
        Task task = project.tasks.add(TASK_CONFIGURE_NAME)
        task.description = "Sets default configuration values for the extension."
        task.group = GROUP
        task.doFirst {
            project.macAppBundle.configureDefaults(project)
        }
        return task
    }

    private Task addCreateInfoPlistTask(Project project) {
        Task task = project.tasks.add(TASK_INFO_PLIST_GENERATE_NAME, GenerateInfoPlistTask)
        task.description = "Creates the Info.plist configuration file inside the mac osx .app directory."
        task.group = GROUP
        task.inputs.property("project version", project.version)
        task.inputs.property("MacAppBundlePlugin extension", {project.macAppBundle})
        task.outputs.file(project.file(project.macAppBundle.getPlistFileForProject(project)))
        return task
    }

    private Task addCopyToLibTask(Project project) {
        Sync task = project.tasks.add(TASK_LIB_COPY_NAME, Sync)
        task.description = "Copies the project dependency jars in the Contents/Resorces/Java directory."
        task.group = GROUP
        task.with configureDistSpec(project)
        task.into { project.file("${project.buildDir}/${project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/Resources/Java") }
        return task
    }

    private Task addCopyStubTask(Project project) {
        Task task = project.tasks.add(TASK_COPY_STUB_NAME, CopyJavaStubTask)
        task.description = "Copies the JavaApplicationStub into the Contents/MacOS directory."
        task.group = GROUP
        task.doLast { ant.chmod(dir: project.file("${project.buildDir}/${project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/MacOS"), perm: "755", includes: "*") }
        task.inputs.property("bundle executable name", {project.macAppBundle.bundleExecutable})
        task.outputs.file("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/MacOS/${project.macAppBundle.bundleExecutable}")
        return task
    }

    private Task addCopyIconTask(Project project) {
        Task task = project.tasks.add(TASK_COPY_ICON_NAME, Copy)
        task.description = "Copies the icon into the Contents/MacOS directory."
        task.group = GROUP
        task.from "${->project.macAppBundle.icon}"
        task.into "${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/Resources"
        return task
    }

    private Task createPkgInfoTask(Project project) {
        Task task = project.tasks.add(TASK_PKG_INFO_GENERATE_NAME, PkgInfoTask)
        task.description = "Creates the Info.plist configuration file inside the mac osx .app directory."
        task.group = GROUP
        task.inputs.property("creator code", { project.macAppBundle.creatorCode } )
        task.outputs.file(project.macAppBundle.getPkgInfoFileForProject(project))
        return task
    }

    private Task addSetFileTask(Project project) {
        def task = project.tasks.add(TASK_SET_FILE_NAME, Exec)
        task.description = "Runs SetFile to toggle the magic bit on the .app (probably not needed)"
        task.group = GROUP
        task.doFirst {
            workingDir = project.file("${project.buildDir}/${project.macAppBundle.appOutputDir}")
            commandLine "${->project.macAppBundle.setFileCmd}", "-a", "B", "${->project.macAppBundle.appName}.app"
        }
        task.inputs.dir("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app")
        task.outputs.dir("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app")
        return task
    }

    private Task addCodeSignTask(Project project) {
        def task = project.tasks.add(TASK_CODE_SIGN_NAME, Exec)
        task.description = "Runs codesign on the .app (not required)"
        task.group = GROUP
        task.doFirst {
            if ( ! project.macAppBundle.certIdentity) {
                throw new InvalidUserDataException("No value has been specified for property certIdentity")
            }
            workingDir = project.file("${project.buildDir}/${project.macAppBundle.appOutputDir}")
            commandLine "${->project.macAppBundle.codeSignCmd}", "-s", "${->project.macAppBundle.certIdentity}", "-f", "${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app"
            if (project.macAppBundle.keyChain) {
                commandLine << "--keychain" << "${->project.macAppBundle.keyChain}"
            }
        }
        task.inputs.dir("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app")
        task.outputs.dir("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app")
        return task
    }

    private Task addDmgTask(Project project) {
        def task = project.tasks.add(TASK_CREATE_DMG, Exec)
        task.description = "Create a dmg containing the .app"
        task.group = GROUP
        task.doFirst {
            workingDir = project.file("${project.buildDir}/${project.macAppBundle.dmgOutputDir}")
            commandLine "hdiutil", "create", "-srcfolder",
                    project.file("${project.buildDir}/${project.macAppBundle.appOutputDir}"),
                    "-volname", "${->project.macAppBundle.volumeName}",
                    "${->project.macAppBundle.dmgName}"
            def dmgFile = project.file("${project.buildDir}/${project.macAppBundle.dmgOutputDir}/${->project.macAppBundle.dmgName}.dmg")
            if (dmgFile.exists()) dmgFile.delete()
        }
        task.inputs.dir("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app")
        task.outputs.file("${->project.buildDir}/${project.macAppBundle.dmgOutputDir}/${->project.macAppBundle.dmgName}.dmg")
        task.doFirst { task.outputs.files.each { it.delete() } }
        task.doFirst { project.file("${->project.buildDir}/${project.macAppBundle.dmgOutputDir}").mkdirs()}
        return task
    }

    private Task addCreateAppTask(Project project) {
        def task = project.tasks.add(TASK_CREATE_APP_NAME)
        task.description = "Placeholder task for tasks relating to creating .app applications"
        task.group = GROUP
        return task
    }

    private CopySpec configureDistSpec(Project project) {
        CopySpec distSpec = project.copySpec {}
        def jar = project.tasks[JavaPlugin.JAR_TASK_NAME]

        distSpec.with {
            from(jar)
            from(project.configurations.runtime)
        }

        return distSpec
    }
}





