package edu.sc.seis.gradle.macAppBundle

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import groovy.xml.MarkupBuilder
import org.gradle.api.plugins.JavaPlugin

class GenerateInfoPlistTask  extends DefaultTask {

    static final String XML_DEF_LINE = '<?xml version="1.0" encoding="UTF-8"?>';
    static final String DOCTYPE_LINE = '<!DOCTYPE plist SYSTEM "file://localhost/System/Library/DTDs/PropertyList.dtd">'
    
    
    @OutputFile
    File getPlistFile() {
        return project.macAppBundle.getPlistFileForProject(project)
    }
    
    @TaskAction
    def void writeInfoPlist() {
        MacAppBundlePluginExtension extension = project.macAppBundle
        def classpath = project.configurations.runtime.collect { "${it.name}" }
        classpath.add(project.tasks[JavaPlugin.JAR_TASK_NAME].outputs.files.getSingleFile().name)
        def file = getPlistFile()
        file.parentFile.mkdirs()
        def writer = new BufferedWriter(new FileWriter(file))
        writer.write(XML_DEF_LINE);writer.newLine();
        writer.write(DOCTYPE_LINE);writer.newLine();
        def xml = new MarkupBuilder(writer)
        xml.plist(version:"0.9") {
            dict() {
                key('CFBundleName')
                string(project.name)
                key('CFBundleIdentifier')
                string(extension.mainClassName)
                key('CFBundleVersion')
                string(project.version)
                key('CFBundleAllowMixedLocalizations')
                if (extension.bundleAllowMixedLocalizations) { string('true') } else { string('false') }
                key('CFBundleExecutable')
                string(extension.bundleExecutable)
                key('CFBundleDevelopmentRegion')
                string(extension.bundleDevelopmentRegion)
                key('CFBundlePackageType')
                string(extension.bundlePackageType)
                key('CFBundleSignature')
                string(extension.creatorCode)
                key('CFBundleInfoDictionaryVersion')
                string(extension.bundleInfoDictionaryVersion)
                key('CFBundleIconFile')
                string(project.file(extension.icon).name)
                key('Java')
                dict() {
                    key('MainClass')
                    string(extension.mainClassName)
                    key('JVMVersion')
                    string(extension.jvmVersion)
                    key('ClassPath')
                    array() {
                        classpath.sort().each() { val -> string('$JAVAROOT/'+val ) }
                    }
                    key('Properties')
                    dict {
                        key('apple.laf.useScreenMenuBar')
                        string(extension.useScreenMenuBar)
                    }
                }
                if (extension.extras != null) {
                    xml.getPrinter().with { p -> p.println(extension.extras) }
                }
            }
        }
        writer.close()
    }
    
}
