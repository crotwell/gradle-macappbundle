package edu.sc.seis.macAppBundle

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;


class PkgInfoTask  extends DefaultTask {

    static final String APPL_LINE = 'APPL';


    @OutputFile
    File getPkgInfoFile() {
        return project.file("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/PkgInfo")
    }

    @TaskAction
    def void writeInfoPlist() {
        def file = getPkgInfoFile()
        file.parentFile.mkdirs()
        def writer = new BufferedWriter(new FileWriter(file))
        writer.write(APPL_LINE)
        writer.write(project.macAppBundle.creatorCode);
        writer.newLine();
        writer.close();
    }
}
