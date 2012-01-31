package edu.sc.seis.gradle.macAppBundle

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;


class PkgInfoTask  extends DefaultTask {

    static final String APPL_LINE = 'APPL';


    @OutputFile
    File getPkgInfoFile() {
        return project.macAppBundle.getPkgInfoFileForProject(project)
    }

    @TaskAction
    def void writeInfoPlist() {
        MacAppBundlePluginExtension extension = project.macAppBundle
        def file = getPkgInfoFile()
        file.parentFile.mkdirs()
        def writer = new BufferedWriter(new FileWriter(file))
        writer.write(APPL_LINE)
        writer.write(extension.creatorCode);
        writer.newLine();
        writer.close();
    }
}