
package edu.sc.seis.gradle.macAppBundle

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

class ExecSetFileTask extends DefaultTask {


    @TaskAction
    def void runSetFile() {
        MacAppBundlePluginExtension pluginExtension = project.macAppBundle
        Process procEcho = [project.macAppBundle.setFileCmd, "-a", "B", "${project.name}.app"].execute(null, project.file("${project.buildDir}/${project.macAppBundle.outputDir}"))
        procEcho.consumeProcessErrorStream(System.err)
        procEcho.consumeProcessOutputStream(System.out)
        if (procEcho.waitFor() != 0) {
            throw new RuntimeException(project.macAppBundle.setFileCmd+' exec failed')
        }
        println ""
    }

}

