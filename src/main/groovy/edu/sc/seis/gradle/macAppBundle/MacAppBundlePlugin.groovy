package edu.sc.seis.gradle.macAppBundle;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.CopySpec;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.bundling.Zip;
import groovy.text.SimpleTemplateEngine;


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
    static final String TASK_BUNDLE_JRE_NAME = "bundleJRE"
    static final String TASK_CREATE_APP_NAME = "createApp"
    static final String TASK_CODE_SIGN_NAME = "codeSign"
    static final String TASK_COPY_BKGD_IMAGE_NAME = "copyBackgroundImage"
    static final String TASK_CREATE_DMG = "createDmg"
    static final String TASK_CREATE_ZIP = "createAppZip"


    def void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        MacAppBundlePluginExtension pluginExtension = new MacAppBundlePluginExtension()
        project.extensions.macAppBundle = pluginExtension
        project.afterEvaluate {
            // this needs to happen after the extension has been populated, but
            // before any tasks run
            pluginExtension.configureDefaults(project)
        }

        Task plistTask = addCreateInfoPlistTask(project)
        Task copyTask = addCopyToLibTask(project)
        Task stubTask = addCopyStubTask(project)
        Task copyIconTask = addCopyIconTask(project)
        Task pkgInfoTask = createPkgInfoTask(project)
        Task bundleJRETask = createBundleJRETask(project)
        Task createAppTask = addCreateAppTask(project)
        createAppTask.dependsOn(plistTask)
        createAppTask.dependsOn(copyTask)
        createAppTask.dependsOn(stubTask)
        createAppTask.dependsOn(copyIconTask)
        createAppTask.dependsOn(pkgInfoTask)
        createAppTask.dependsOn(bundleJRETask)
        Task setFileTask = addSetFileTask(project)
        setFileTask.dependsOn(createAppTask)
        /* I think setfile is not required for a .app to be run on osx.
         * Leaving the task in, but not depended on by anything else. If
         * SetFile is needed, then switch the above depends to
         createAppTask.dependsOn(setFileTask)
         */
        setFileTask.mustRunAfter(createAppTask)
        Task codeSignTask = addCodeSignTask(project)
        codeSignTask.dependsOn(createAppTask)
        Task copyBkgImage = createCopyBackgroundImageTask(project)
        Task dmgTask = createDMGTask(project)
        dmgTask.dependsOn(copyBkgImage)
        //dmgTask.dependsOn(dmgTask)
        dmgTask.dependsOn(createAppTask)
        dmgTask.dependsOn(copyBkgImage)
        dmgTask.mustRunAfter codeSignTask
        dmgTask.mustRunAfter setFileTask
        Task zipTask = createAppZipTask(project)
        zipTask.dependsOn(createAppTask)
        zipTask.mustRunAfter codeSignTask
        zipTask.mustRunAfter setFileTask
        project.getTasksByName("assemble", true).each{ t -> t.dependsOn(dmgTask) }
    }

    private Task addCreateInfoPlistTask(Project project) {
        Task task = project.tasks.create(TASK_INFO_PLIST_GENERATE_NAME, GenerateInfoPlistTask)
        task.description = "Creates the Info.plist configuration file inside the mac osx .app directory."
        task.group = GROUP
        task.inputs.property("project version", {project.version})
        task.inputs.property("MacAppBundlePlugin extension", {project.macAppBundle})
        return task
    }

    private Task addCopyToLibTask(Project project) {
        Sync task = project.tasks.create(TASK_LIB_COPY_NAME, Sync)
        task.description = "Copies the project dependency jars in the Contents/Resorces/Java directory."
        task.group = GROUP
        task.with configureDistSpec(project)
        task.into { project.file("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/${->project.macAppBundle.jarSubdir}") }
        return task
    }

    private Task addCopyStubTask(Project project) {
        Task task = project.tasks.create(TASK_COPY_STUB_NAME, CopyJavaStubTask)
        task.description = "Copies the JavaApplicationStub into the Contents/MacOS directory."
        task.group = GROUP
        task.doLast { ant.chmod(dir: project.file("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/MacOS"), perm: "755", includes: "*") }
        task.inputs.property("bundle executable name", {project.macAppBundle.bundleExecutable})
        task.outputs.file("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/MacOS/${->project.macAppBundle.bundleExecutable}")
        return task
    }

    private Task addCopyIconTask(Project project) {
        Task task = project.tasks.create(TASK_COPY_ICON_NAME, Copy)
        task.description = "Copies the icon into the Contents/MacOS directory."
        task.group = GROUP
        task.from "${->project.macAppBundle.icon}"
        task.into "${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/Resources"
        return task
    }

    private Task createPkgInfoTask(Project project) {
        Task task = project.tasks.create(TASK_PKG_INFO_GENERATE_NAME, PkgInfoTask)
        task.description = "Creates the PkgInfo configuration file inside the mac osx .app directory."
        task.group = GROUP
        task.inputs.property("creator code", { project.macAppBundle.creatorCode } )
        return task
    }

    private Task createBundleJRETask(Project project) {
        Task task = project.tasks.create(TASK_BUNDLE_JRE_NAME, Sync)
        task.description = "Copies the JRE into the Contents/PlugIns directory."
        task.group = GROUP
        task.from("${->project.macAppBundle.jreHome}/../..") {
            include('Contents/Home/jre/**')
            include('Contents/Info.plist')
            exclude('Contents/Home/jre/bin')
            exclude('Contents/Home/"bin/')
            exclude('Contents/Home/jre/bin/')
            exclude('Contents/Home/jre/lib/deploy/')
            exclude('Contents/Home/jre/lib/deploy.jar')
            exclude('Contents/Home/jre/lib/javaws.jar')
            exclude('Contents/Home/jre/lib/libdeploy.dylib')
            exclude('Contents/Home/jre/lib/libnpjp2.dylib')
            exclude('Contents/Home/jre/lib/plugin.jar')
            exclude('Contents/Home/jre/lib/security/javaws.policy')
        }
        task.into "${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/PlugIns/${->project.macAppBundle.getJREDirName()}"
        task.onlyIf { project.macAppBundle.bundleJRE }
        return task
    }

    private Task addSetFileTask(Project project) {
        def task = project.tasks.create(TASK_SET_FILE_NAME, Exec)
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
        def task = project.tasks.create(TASK_CODE_SIGN_NAME, Exec)
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

    private Task createAppZipTask(Project project) {
        def task = project.tasks.create(TASK_CREATE_ZIP, Zip)
        task.description = "Create a zip containing the .app"
        task.group = GROUP
        // delay configure of task until extension is populated
        project.afterEvaluate {
            task.destinationDir = project.file("${project.buildDir}/${project.macAppBundle.dmgOutputDir}")
            task.from("${->project.buildDir}/${->project.macAppBundle.appOutputDir}") {
                include "${->project.macAppBundle.appName}.app/**"
                exclude "${->project.macAppBundle.appName}.app/Contents/MacOS"
            }
            task.from("${->project.buildDir}/${->project.macAppBundle.appOutputDir}") {
                include "${->project.macAppBundle.appName}.app/Contents/MacOS/**"
                fileMode 0777  // octal requires leading zero
            }
            task.archiveName = "${->project.macAppBundle.dmgName}.zip"
        }
        return task
    }



    private Task createCopyBackgroundImageTask(Project project) {
        Task task = project.tasks.create(TASK_COPY_BKGD_IMAGE_NAME, Sync)
        task.description = "Copies the dmg background image into the .app parent directory, as .background/imagename."
        task.group = GROUP

        project.afterEvaluate {
            task.onlyIf { project.macAppBundle.backgroundImage != null }
            if (project.macAppBundle.backgroundImage != null) {
                task.from "${->project.macAppBundle.backgroundImage}"
                task.into "${->project.buildDir}/${->project.macAppBundle.appOutputDir}/.background"
            }
        }
        return task
    }

    private Task createDMGTask(Project project) {
        def task = project.tasks.create(TASK_CREATE_DMG, Exec)
        task.description = "Create a dmg containing the .app and optional background image"
        task.group = GROUP
        task.inputs.dir("${->project.buildDir}/${->project.macAppBundle.appOutputDir}")
        task.inputs.property("backgroundImage", { project.macAppBundle.backgroundImage } )
        task.outputs.file("${->project.buildDir}/${->project.macAppBundle.dmgOutputDir}/${->project.macAppBundle.dmgName}.dmg")

        project.afterEvaluate {
            def dmgOutDir = project.file("${project.buildDir}/${project.macAppBundle.dmgOutputDir}")
            GString tmpDmgName;
            String dmgFormat;
            if (project.macAppBundle.backgroundImage != null) {
                // if we have a background image, we need to create a RW disk image so we can set it,
                // and then convert the RW to a compressed RO image later with the final name
                tmpDmgName = "tmp_${->project.macAppBundle.dmgName}.dmg"
                dmgFormat = "UDRW"
            } else {
                // in this case, we create the disk image in one go without the conversion step
                tmpDmgName = "${->project.macAppBundle.dmgName}.dmg"
                dmgFormat = "UDZO";
            }
            task.doFirst {
                def dmgFile = new File(dmgOutDir, tmpDmgName)
                if (dmgFile.exists()) {
                    dmgFile.delete()
                }
                def finalDmgFile = new File(dmgOutDir, "${->project.macAppBundle.dmgName}.dmg")
                if (finalDmgFile.exists()) {
                    finalDmgFile.delete()
                }
                workingDir = dmgOutDir
                commandLine "hdiutil", "create", "-srcfolder",
                        project.file("${project.buildDir}/${project.macAppBundle.appOutputDir}"),
                        "-format", "${->dmgFormat}", "-fs", "HFS+",
                        "-volname", "${->project.macAppBundle.volumeName}",
                        tmpDmgName
            }
            task.doLast {
                if (project.macAppBundle.backgroundImage != null) {
                    String backgroundImage = new File(project.macAppBundle.backgroundImage).getName() // just name, not paths
                    doBackgroundImageAppleScript(dmgOutDir, tmpDmgName, "${->project.macAppBundle.dmgName}.dmg", "${->project.macAppBundle.volumeName}", backgroundImage, "${->project.macAppBundle.appName}")
                }
            }
            task.doFirst { task.outputs.files.each { it.delete() } }
            task.doFirst { dmgOutDir.mkdirs() }
        }
        return task
    }

    private Task addCreateAppTask(Project project) {
        def task = project.tasks.create(TASK_CREATE_APP_NAME)
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


    /** see 
     http://asmaloney.com/2013/07/howto/packaging-a-mac-os-x-application-using-a-dmg/
     */
    private void doBackgroundImageAppleScript(File dmgOutDir,
                                              String tmpDmgFile,
                                              String finalDmgFile,
                                              String volMountPoint,
                                              String backgroundImage,
                                              String appName) {
        if (new File("/Volumes/${volMountPoint}").exists()) {
            // if volume already mounted, maybe due to previous build, unmount
            runCmd("hdiutil detach /Volumes/${volMountPoint}", "Unable to detach volume: ${volMountPoint}")
        }
        // mount temp dmg
        def mountCmdText = "hdiutil attach -readwrite -noverify ${dmgOutDir}/${tmpDmgFile}"

        String mountCmdOut = runCmd(mountCmdText, "Unable to mount dmg")
        if ( ! new File("/Volumes/${volMountPoint}").exists()) {
            throw new RuntimeException("Unable to find volume mount point to set background image. ${volMMountPoint}")
        }

        runCmd("ln -s /Applications /Volumes/${volMountPoint}", "Unable to link /Applications in dmg");

        def binding = ["APP_NAME":appName, "VOL_NAME":volMountPoint, "DMG_BACKGROUND_IMG":backgroundImage ]
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate(backgroundScript).make(binding)

        sleep 2000
        def appleScriptCmd = "osascript".execute()
        appleScriptCmd.withWriter { writer ->
            writer << template.toString()
        }
        appleScriptCmd.waitFor();
        def retCode = appleScriptCmd.exitValue();
        if (retCode != 0) {
            throw new RuntimeException("Problem running applescript to set dmg background image: "+retCode+" "+appleScriptCmd.err.text);
        }
        runCmd("hdiutil detach /Volumes/${volMountPoint}", "Unable to detach volume: ${volMountPoint}")
        runCmd("hdiutil convert ${dmgOutDir}/${tmpDmgFile} -format UDZO -imagekey zlib-level=9 -o ${dmgOutDir}/${finalDmgFile}", "Unable to convert dmg image")
        new File("${dmgOutDir}/${tmpDmgFile}").delete()
    }

    private String runCmd(GString cmdText, String errMsg) {
        return runCmd(cmdText, GString.EMPTY + errMsg);
    }

    private String runCmd(GString cmdText, GString errMsg) {
        def cmd = cmdText.execute()
        cmd.waitFor();
        def retCode = cmd.exitValue();
        if (retCode != 0) {
            throw new RuntimeException("${errMsg}, return code from '${cmdText}' is nonzero: ${retCode}  ${cmd.err.text}");
        }
        return cmd.in.text
    }

    String backgroundScript = """
   tell application "Finder"
     tell disk "\${VOL_NAME}"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 920, 440}
           set viewOptions to the icon view options of container window
           set arrangement of viewOptions to not arranged
           set icon size of viewOptions to 72
           set background picture of viewOptions to file ".background:\${DMG_BACKGROUND_IMG}"
           set position of item "\${APP_NAME}.app" of container window to {160, 205}
           set position of item "Applications" of container window to {360, 205}
           close
           open
           update without registering applications
           delay 2
        end tell
     end tell
"""

}





