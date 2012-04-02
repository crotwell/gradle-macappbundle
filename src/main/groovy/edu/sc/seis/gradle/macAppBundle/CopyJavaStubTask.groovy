package edu.sc.seis.gradle.macAppBundle

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


class CopyJavaStubTask extends DefaultTask {


    @TaskAction
    def void writeStub() {
        dest = project.file("${project.buildDir}/${project.macAppBundle.outputDir}/${project.macAppBundle.appName}.app/Contents/MacOS/JavaApplicationStub")
        dest.parentFile.mkdirs()
        outStream = new BufferedOutputStream(new FileOutputStream(dest))
        buf = new byte[1024]
        inStream = this.getClass().getClassLoader().getResourceAsStream("edu/sc/seis/macAppBundle/JavaApplicationStub")
        int numRead = inStream.read(buf)
        while (numRead > 0) {
            outStream.write(buf, 0, numRead)
            numRead = inStream.read(buf)
        }
        inStream.close()
        outStream.close()
    }
}
