package edu.sc.seis.gradle.macAppBundle;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.file.CopySpec
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Sync;


class MacAppBundlePlugin implements Plugin<Project> {

    static final String PLUGIN_NAME = "macAppBundle"
    static final String GROUP = PLUGIN_NAME

    static final String TASK_MKDIR_NAME = "createAppDirs"
    static final String TASK_INFO_PLIST_GENERATE_NAME = "generatePlist"
    static final String TASK_PKG_INFO_GENERATE_NAME = "generatePkgInfo"
    
    static final String TASK_LIB_COPY_NAME = "copyToResourcesJava"
    static final String TASK_COPY_STUB_NAME = "copyStub"
    static final String TASK_SET_FILE_NAME = "runSetFile"
    static final String TASK_CREATE_APP_NAME = "createApp"
    

    def void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        MacAppBundlePluginExtension pluginExtension = new MacAppBundlePluginExtension()
        pluginExtension.initExtensionDefaults(project)
        project.extensions.macAppBundle = pluginExtension
        
        Task plistTask = addCreateInfoPlistTask(project)
        Task copyTask = addCopyToLibTask(project)
        Task stubTask = addCopyStubTask(project)
        Task pkgInfoTask = createPkgInfoTask(project)
        Task setFileTask = addSetFileTask(project)
        setFileTask.dependsOn(plistTask)
        setFileTask.dependsOn(copyTask)
        setFileTask.dependsOn(stubTask)
        setFileTask.dependsOn(pkgInfoTask)
        Task mainTask = addCreateAppTask(project)
        mainTask.dependsOn(setFileTask)
        
    }

    private Task addCreateInfoPlistTask(Project project) {
        Task task = project.tasks.add(TASK_INFO_PLIST_GENERATE_NAME, GenerateInfoPlistTask)
        task.description = "Creates the Info.plist configuration file inside the mac osx .app directory."
        task.group = GROUP
        return task
    }

    private Task addCopyToLibTask(Project project) {
        Sync task = project.tasks.add(TASK_LIB_COPY_NAME, Sync)
        task.description = "Copies the project dependency jars in the Contents/Resorces/Java directory."
        task.group = GROUP
        task.with configureDistSpec(project)
        task.into { project.file("${project.buildDir}/${project.macAppBundle.outputDir}/${project.name}.app/Contents/Resources/Java") }
        return task
    }

    private Task addCopyStubTask(Project project) {
        Sync task = project.tasks.add(TASK_COPY_STUB_NAME, Sync)
        task.description = "Copies the JavaApplicationStub into the Contents/MacOS directory."
        task.group = GROUP
        task.with = "JavaApplicationStub"
        task.from('/System/Library/Frameworks/JavaVM.framework/Versions/Current/Resources/MacOS/')
        task.into { project.file("${project.buildDir}/${project.macAppBundle.outputDir}/${project.name}.app/Contents/MacOS") }
        task.doLast { ant.chmod(dir: project.file("${project.buildDir}/${project.macAppBundle.outputDir}/${project.name}.app/Contents/MacOS"), perm: "755", includes: "*") }
        return task
    }
    
    private Task createPkgInfoTask(Project project) {
        Task task = project.tasks.add(TASK_PKG_INFO_GENERATE_NAME, PkgInfoTask)
        task.description = "Creates the Info.plist configuration file inside the mac osx .app directory."
        task.group = GROUP
        return task
    }

    private Task addSetFileTask(Project project) {
        def run = project.tasks.add(TASK_SET_FILE_NAME, ExecSetFileTask)
        run.description = "Runs SetFile -a B on the .app"
        run.group = GROUP
        return run
    }

    private Task addCreateAppTask(Project project) {
        def run = project.tasks.add(TASK_CREATE_APP_NAME)
        run.description = "Placeholder task for tasks relating to creating .app applications"
        run.group = GROUP
        return run
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





