package edu.sc.seis.macAppBundle;

import org.apache.tools.ant.taskdefs.condition.Os;
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


    void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        def pluginExtension = project.extensions.create("macAppBundle", MacAppBundlePluginExtension)
        project.afterEvaluate {
            // this needs to happen after the extension has been populated, but
            // before any tasks run
            pluginExtension.configureDefaults(project)
            project.getTasks().getByName("copyToResourcesJava").dependsOn(pluginExtension.jarTask)
            project.getTasks().getByName("generatePlist").dependsOn(pluginExtension.jarTask)
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
        project.afterEvaluate{
          if (Os.isFamily(Os.FAMILY_MAC)) {
              project.getTasksByName("assemble", true).each{ t -> t.dependsOn(dmgTask) }
          } else {
              project.getTasksByName("assemble", true).each{ t -> t.dependsOn(zipTask) }
          }
        }
    }

    private Task addCreateInfoPlistTask(Project project) {
        Task task = project.tasks.create(TASK_INFO_PLIST_GENERATE_NAME, GenerateInfoPlistTask)
        task.description = "Creates the Info.plist configuration file inside the mac osx .app directory."
        task.group = GROUP
        task.inputs.property("project version", {project.version})
        //task.inputs.property("MacAppBundlePlugin extension", {project.macAppBundle})
        task.inputs.property("MacAppBundlePlugin appStyle", {project.macAppBundle.appStyle})
        task.inputs.property("MacAppBundlePlugin setFileCmd", {project.macAppBundle.setFileCmd})
        task.inputs.property("MacAppBundlePlugin appOutputDir", {project.macAppBundle.appOutputDir})
        task.inputs.property("MacAppBundlePlugin dmgOutputDir", {project.macAppBundle.dmgOutputDir})
        task.inputs.property("MacAppBundlePlugin mainClassName", {project.macAppBundle.mainClassName})
        task.inputs.property("MacAppBundlePlugin bundleIdentifier", {project.macAppBundle.bundleIdentifier})
        task.inputs.property("MacAppBundlePlugin creatorCode", {project.macAppBundle.creatorCode})
        task.inputs.property("MacAppBundlePlugin runtimeConfigurationName", {project.macAppBundle.runtimeConfigurationName})
        task.inputs.property("MacAppBundlePlugin icon", {project.macAppBundle.icon})
        task.inputs.property("MacAppBundlePlugin jvmVersion", {project.macAppBundle.jvmVersion})
        task.inputs.property("MacAppBundlePlugin appName", {project.macAppBundle.appName})
        task.inputs.property("MacAppBundlePlugin volumeName", {project.macAppBundle.volumeName})
        task.inputs.property("MacAppBundlePlugin dmgName", {project.macAppBundle.dmgName})
        task.inputs.property("MacAppBundlePlugin javaProperties", {project.macAppBundle.javaProperties})
        task.inputs.property("MacAppBundlePlugin javaExtras", {project.macAppBundle.javaExtras})
        task.inputs.property("MacAppBundlePlugin bundleExtras", {project.macAppBundle.bundleExtras})
        task.inputs.property("MacAppBundlePlugin arguments", {project.macAppBundle.arguments})
        task.inputs.property("MacAppBundlePlugin bundleExecutable", {project.macAppBundle.bundleExecutable})
        task.inputs.property("MacAppBundlePlugin bundleAllowMixedLocalizations", {project.macAppBundle.bundleAllowMixedLocalizations})
        task.inputs.property("MacAppBundlePlugin highResolutionCapable", {project.macAppBundle.highResolutionCapable})
        task.inputs.property("MacAppBundlePlugin bundlePackageType", {project.macAppBundle.bundlePackageType})
        task.inputs.property("MacAppBundlePlugin bundleInfoDictionaryVersion", {project.macAppBundle.bundleInfoDictionaryVersion})
        task.inputs.property("MacAppBundlePlugin bundleDevelopmentRegion", {project.macAppBundle.bundleDevelopmentRegion})
        task.inputs.property("MacAppBundlePlugin bundleJRE", {project.macAppBundle.bundleJRE})
        task.inputs.property("MacAppBundlePlugin jreHome", {project.macAppBundle.jreHome})
        task.inputs.property("MacAppBundlePlugin codeSignCmd", {project.macAppBundle.codeSignCmd})
        task.inputs.property("MacAppBundlePlugin codeSignDeep", {project.macAppBundle.codeSignDeep})
        task.inputs.property("MacAppBundlePlugin backgroundScript", {project.macAppBundle.backgroundScript})
        task.inputs.property("MacAppBundlePlugin appIconX", {project.macAppBundle.appIconX})
        task.inputs.property("MacAppBundlePlugin appIconY", {project.macAppBundle.appIconY})
        task.inputs.property("MacAppBundlePlugin appFolderX", {project.macAppBundle.appFolderX})
        task.inputs.property("MacAppBundlePlugin appFolderY", {project.macAppBundle.appFolderY})
        task.inputs.property("MacAppBundlePlugin backgroundImageWidth", {project.macAppBundle.backgroundImageWidth})
        task.inputs.property("MacAppBundlePlugin backgroundImageHeight", {project.macAppBundle.backgroundImageHeight})

        // optional inputs
        //     code below should be changed to 
        //     task.inputs.property("...", {...}).optional(true)
        //     once minimal supported Gradle version is 4.3
        if(project.macAppBundle.backgroundImage) task.inputs.property("MacAppBundlePlugin backgroundImage", {project.macAppBundle.backgroundImage})
        if(project.macAppBundle.certIdentity) task.inputs.property("MacAppBundlePlugin certIdentity", {project.macAppBundle.certIdentity})
        if(project.macAppBundle.keyChain) task.inputs.property("MacAppBundlePlugin keyChain", {project.macAppBundle.keyChain})
        
        return task
    }

    private Task addCopyToLibTask(Project project) {
        Sync task = project.tasks.create(TASK_LIB_COPY_NAME, Sync)
        task.description = "Copies the project dependency jars in the ${->project.macAppBundle.jarSubdir} directory, usually Contents/Java)."
        task.group = GROUP
        project.afterEvaluate {
          task.with configureDistSpec(project)
        }
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
       
        project.afterEvaluate {
            if (project.macAppBundle.bundleJRE && project.macAppBundle.getJreHome() != null) {
                task.from("${->project.macAppBundle.getJreHome()}/../..") {
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
            }
            task.onlyIf { project.macAppBundle.bundleJRE }
        }
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
            //commandLine "${->project.macAppBundle.codeSignCmd}", "--deep" , "-s" , "${->project.macAppBundle.certIdentity}" , "-f" , "${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app" 

            def tmpCommandLine = [ "${->project.macAppBundle.codeSignCmd}", "-s" , "${->project.macAppBundle.certIdentity}" , "-f" , "${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app" ];
            if (project.macAppBundle.codeSignDeep) {
                tmpCommandLine << "--deep"
            }
            if (project.macAppBundle.keyChain) {
                tmpCommandLine << "--keychain" << "${->project.macAppBundle.keyChain}"
            }
            commandLine tmpCommandLine
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
            task.doLast {
                String backgroundImage = new File(project.macAppBundle.backgroundImage).getName() // just name, not paths    
                def imageWidth = runCmd(["sips", "-g", "pixelWidth", "${->project.buildDir}/${->project.macAppBundle.appOutputDir}/.background/${backgroundImage}"], "Unable to determine image size with sips")
                imageWidth = imageWidth.tokenize().last();
                def imageHeight = runCmd(["sips", "-g", "pixelHeight", "${->project.buildDir}/${->project.macAppBundle.appOutputDir}/.background/${backgroundImage}"], "Unable to determine image size with sips")
                imageHeight = imageHeight.tokenize().last();
                project.macAppBundle.backgroundImageWidth = imageWidth
                project.macAppBundle.backgroundImageHeight = imageHeight
            }
        }
        return task
    }

    private Task createDMGTask(Project project) {
        def task = project.tasks.create(TASK_CREATE_DMG, Exec)
        task.description = "Create a dmg containing the .app and optional background image"
        task.group = GROUP
        task.inputs.dir("${->project.buildDir}/${->project.macAppBundle.appOutputDir}")

        // optional inputs
        //     code below should be changed to 
        //     task.inputs.property("...", {...}).optional(true)
        //     once minimal supported Gradle version is 4.3
        // task.inputs.property("backgroundImage", { project.macAppBundle.backgroundImage } ).optional(true)
        if(project.macAppBundle.backgroundImage) task.inputs.property("MacAppBundlePlugin backgroundImage", {project.macAppBundle.backgroundImage})
        
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
                    doBackgroundImageAppleScript(dmgOutDir,
                                                 tmpDmgName,
                                                  "${->project.macAppBundle.dmgName}.dmg",
                                                   "${->project.macAppBundle.volumeName}",
                                                    backgroundImage,
                                                    "${->project.macAppBundle.appName}",
                                                    "${->project.macAppBundle.backgroundScript}",
                                                    "${->project.macAppBundle.backgroundImageWidth}",
                                                    "${->project.macAppBundle.backgroundImageHeight}",
                                                    "${->project.macAppBundle.appIconX}",
                                                    "${->project.macAppBundle.appIconY}",
                                                    "${->project.macAppBundle.appFolderX}",
                                                    "${->project.macAppBundle.appFolderY}")
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
        def jar = project.tasks[project.macAppBundle.jarTask]
        distSpec.with {
            from(jar)
            from(project.configurations[project.macAppBundle.runtimeConfigurationName])
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
                                              String appName,
                                              String backgroundScript,
                                              String imageWidth,
                                              String imageHeight,
                                              String appIconX, String appIconY,
                                              String appFolderX, String appFolderY) {
        if (new File("/Volumes/${volMountPoint}").exists()) {
            // if volume already mounted, maybe due to previous build, unmount
            runCmd(["hdiutil", "detach", "/Volumes/${volMountPoint}"], "Unable to detach volume: ${volMountPoint}")
        }

        // mount temp dmg
        String mountCmdOut = runCmd(["hdiutil", "attach", "-readwrite", "-noverify", "${dmgOutDir}/${tmpDmgFile}"], "Unable to mount dmg")
        if ( ! new File("/Volumes/${volMountPoint}").exists()) {
            throw new RuntimeException("Unable to find volume mount point to set background image. ${volMountPoint}")
        }

        runCmd(["ln", "-s", "/Applications", "/Volumes/${volMountPoint}"], "Unable to link /Applications in dmg");
        
        def binding = ["APP_NAME":appName, "VOL_NAME":volMountPoint, "DMG_BACKGROUND_IMG":backgroundImage,
            "IMAGE_WIDTH":imageWidth,
            "IMAGE_HEIGHT":imageHeight,
            "APPICONX":appIconX, "APPICONY":appIconY,
            "APPFOLDERX":appFolderX, "APPFOLDERY":appFolderY ]
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate(backgroundScript).make(binding)

//        sleep 2000
        def appleScriptCmd = "osascript".execute()
        appleScriptCmd.withWriter { writer ->
            writer << template.toString()
        }
        appleScriptCmd.waitFor();
        def retCode = appleScriptCmd.exitValue();
        if (retCode != 0) {
            throw new RuntimeException("Problem running applescript to set dmg background image: "+retCode+" "+appleScriptCmd.err.text);
        }
        runCmd(["hdiutil", "detach", "/Volumes/${volMountPoint}"], "Unable to detach volume: ${volMountPoint}")
        runCmd(["hdiutil", "convert", "${dmgOutDir}/${tmpDmgFile}", "-format", "UDZO", "-imagekey", "zlib-level=9", "-o", "${dmgOutDir}/${finalDmgFile}"], "Unable to convert dmg image")
        new File("${dmgOutDir}/${tmpDmgFile}").delete()
    }

    private String runCmd(List cmd, String errMsg) {
        def sout = new StringBuffer()
        def serr = new StringBuffer()
        def cmdProc = cmd.execute()
        cmdProc.consumeProcessOutput(sout, serr)
        cmdProc.waitFor();
        def retCode = cmdProc.exitValue();
        if (retCode != 0) {
            throw new RuntimeException("${errMsg}, return code from '${cmd.join(' ')}' is nonzero: ${retCode}  ${serr}");
        }
        return sout
    }

}





