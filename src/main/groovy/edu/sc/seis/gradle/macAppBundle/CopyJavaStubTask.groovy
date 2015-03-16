package edu.sc.seis.gradle.macAppBundle

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


class CopyJavaStubTask extends DefaultTask {


    @TaskAction
    def void writeStub() {
        def dest = project.file("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/MacOS/${project.macAppBundle.bundleExecutable}")
        dest.parentFile.mkdirs()
        def outStream = new BufferedOutputStream(new FileOutputStream(dest))
        def buf = new byte[1024]
        Class thisClass = edu.sc.seis.gradle.macAppBundle.CopyJavaStubTask.class;
        def url = thisClass.getClassLoader().getResource("edu/sc/seis/gradle/macAppBundle/${->project.macAppBundle.bundleExecutable}");
        
        def JarURLConnection conn = url.openConnection();
        conn.connect();
        def inStream = url.openStream();
        if (inStream == null) throw new RuntimeException("Can't find resource for ${->project.macAppBundle.bundleExecutable} in jar "+url+"  "+thisClass.getName())
        int numRead = inStream.read(buf)
        while (numRead > 0) {
            outStream.write(buf, 0, numRead)
            numRead = inStream.read(buf)
        }
        inStream.close()
        outStream.close()
    }
}
