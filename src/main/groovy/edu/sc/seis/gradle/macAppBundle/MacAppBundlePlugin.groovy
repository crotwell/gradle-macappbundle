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
    static final String TASK_LIB_COPY_NAME = "copyResources"
    static final String TASK_COPY_STUB_NAME = "copyStub"
    static final String TASK_RUN_NAME = "createApp"
    

    def void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        MacAppBundlePluginExtension pluginExtension = new MacAppBundlePluginExtension()
        pluginExtension.initExtensionDefaults(project)
        project.extensions.macAppBundle = pluginExtension
        
        Task xmlTask = addCreateInfoPlistTask(project)
        Task copyTask = addCopyToLibTask(project)
        Task stubTask = addCopyStubTask(project)
        //Task runTask = addRunLauch4jTask(project)
        //runTask.dependsOn(copyTask)
        //runTask.dependsOn(xmlTask)
        
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
        task.into { project.file("${project.buildDir}/${project.macAppBundle.outputDir}//${project.name}.app/Contents/Resorces/Java") }
        return task
    }

    private Task addCopyStubTask(Project project) {
        Sync task = project.tasks.add(TASK_COPY_STUB_NAME, Sync)
        task.description = "Copies the JavaApplicationStub into the Contents/MacOS directory."
        task.group = GROUP
        task.with = "JavaApplicationStub"
        task.from('/System/Library/Frameworks/JavaVM.framework/Versions/Current/Resources/MacOS/')
        task.into { project.file("${project.buildDir}/${project.macAppBundle.outputDir}//${project.name}.app/Contents/MacOS") }
        return task
    }
/*
    private Task addRunLauch4jTask(Project project) {
        def run = project.tasks.add(TASK_RUN_NAME, ExecLaunch4JTask)
        run.description = "Runs launch4j to generate an .exe file"
        run.group = GROUP
        return run
    }
*/
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





