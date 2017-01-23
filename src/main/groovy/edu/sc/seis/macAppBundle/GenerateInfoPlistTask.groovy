package edu.sc.seis.macAppBundle

import java.io.File;
import java.text.SimpleDateFormat;

import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import groovy.xml.MarkupBuilder
import org.gradle.api.plugins.JavaPlugin

class GenerateInfoPlistTask  extends DefaultTask {

    static final String XML_DEF_LINE = '<?xml version="1.0" encoding="UTF-8"?>';
    static final String DOCTYPE_LINE = '<!DOCTYPE plist SYSTEM "file://localhost/System/Library/DTDs/PropertyList.dtd">'
    static final String URL_DOCTYPE_LINE = '<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">'
    static final String SHAMELESS_PROMO = '<!-- created with Gradle, http://gradle.org, and the MacAppBundle plugin, http://code.google.com/p/gradle-macappbundle -->'

    @OutputFile
    File getPlistFile() {
        return project.file("${->project.buildDir}/${->project.macAppBundle.appOutputDir}/${->project.macAppBundle.appName}.app/Contents/Info.plist")
    }

    @TaskAction
    def void writeInfoPlist() {
        if (project.macAppBundle.appStyle == 'Oracle') {
            writeInfoPlistOracleJava();
        } else {
            writeInfoPlistAppleJava();
        }
    }
    
    def void writeInfoPlistOracleJava() {
        MacAppBundlePluginExtension extension = project.macAppBundle
        def file = getPlistFile()
        file.parentFile.mkdirs()
        def writer = new BufferedWriter(new FileWriter(file))
        writer.writeLine(XML_DEF_LINE);
        writer.writeLine(URL_DOCTYPE_LINE);
        writer.writeLine(SHAMELESS_PROMO);
        def xml = new MarkupBuilder(writer)
        xml.plist(version:"1.0") {
            dict() {
                key('CFBundleDevelopmentRegion')
                string(extension.bundleDevelopmentRegion)
                key('CFBundleExecutable')
                string(extension.bundleExecutable)
                key('CFBundleIconFile')
                string(project.file(extension.icon).name)
                key('CFBundleIdentifier')
                string(extension.bundleIdentifier)
                
                key('CFBundleInfoDictionaryVersion')
                string(extension.bundleInfoDictionaryVersion)
                key('CFBundleName')
                string(extension.appName)
                key('CFBundlePackageType')
                string(extension.bundlePackageType)
                
                key('CFBundleVersion')
                string(project.version)
                key('CFBundleShortVersionString')
                string(project.version)
                key('CFBundleAllowMixedLocalizations')
                if (extension.bundleAllowMixedLocalizations) { string('true') } else { string('false') }
                key('NSHighResolutionCapable')
                if (extension.highResolutionCapable) { string('true') } else { string('false') }
                key('CFBundleSignature')
                string(extension.creatorCode)
                if (extension.bundleJRE) {
                    if (extension.getJreHome() != null) {
                        File jreHomeFile = new File(extension.getJreHome());
                        if (jreHomeFile.exists()) {
                            def jreVersion = jreHomeFile.getParentFile().getParentFile().getName()
                            key('JVMRuntime')
                            string(jreVersion)
                        }
                    }
                }
                key('JVMMainClassName')
                string(extension.mainClassName)
                key('JVMOptions')
                array() {
                    extension.javaProperties.each { k, v->
                        if (v != null) {
                            string("-D$k=$v")
                        } else {
                            string("-D$k")
                        }
                    }
                    extension.javaExtras.each { k, v->
                        if (v != null) {
                            string("$k=$v")
                        } else {
                            string("$k")
                        }
                    }
                }
                key('JVMArguments')
                array() {
                    extension.arguments.each { v->
                            string("$v")
                    }
                }
                extension.bundleExtras.each { k, v->
                    key("$k")
                    doValue(xml, v)
                }
            }
        }
        writer.close()
    }
    
    def void writeInfoPlistAppleJava() {
        MacAppBundlePluginExtension extension = project.macAppBundle
        def classpath;
        if (extension.runtimeConfigurationName == null ) {
            classpath = project.configurations.runtime.collect { "${it.name}" }
            classpath.add(project.tasks[JavaPlugin.JAR_TASK_NAME].outputs.files.getSingleFile().name)
        } else {
            classpath = project.configurations[extension.runtimeConfigurationName].collect { "${it.name}" }
        }
        def file = getPlistFile()
        file.parentFile.mkdirs()
        def writer = new BufferedWriter(new FileWriter(file))
        writer.write(XML_DEF_LINE);writer.newLine();
        writer.write(DOCTYPE_LINE);writer.newLine();
        def xml = new MarkupBuilder(writer)
        xml.plist(version:"0.9") {
            dict() {
                key('CFBundleName')
                string(extension.appName)
                key('CFBundleIdentifier')
                string(extension.bundleIdentifier)
                key('CFBundleShortVersionString')
                string(project.version)
                key('CFBundleVersion')
                string(project.version)
                key('CFBundleAllowMixedLocalizations')
                if (extension.bundleAllowMixedLocalizations) { string('true') } else { string('false') }
                key('NSHighResolutionCapable')
                if (extension.highResolutionCapable) { string('true') } else { string('false') }
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
                    doValue(xml, classpath.sort().collect { val -> '$JAVAROOT/'+val  })
                    key('Properties')
                    dict {
                        extension.javaProperties.each { k, v->
                            key("$k")
                            doValue(xml, v)
                        }
                    }
                    extension.javaExtras.each { k, v->
                        key("$k")
                        doValue(xml, v)
                    }
                }
                extension.bundleExtras.each { k, v->
                    key("$k")
                    doValue(xml, v)
                }
            }
        }
        writer.close()
    }

    def doValue(xml, value)  {
        if (value instanceof String)  {
            xml.string("$value")
        } else if (value instanceof Date)  {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            xml.date(sdf.format(value));
            //YYYY-MM-DD HH:MM:SS 
        } else if (value instanceof Short || value instanceof Integer)  {
            xml.integer(value)
        } else if (value instanceof Float || value instanceof Double)  {
            xml.real(value)
        } else if (value instanceof Boolean) {
            if (value) {
                xml.true()
            } else {
                xml.false()
            }
        } else if (value instanceof Map) {
            xml.dict {
                value.each { subk, subv ->
                    key("$subk")
                    doValue(xml, subv)
                }
            }
        } else if (value instanceof List || value instanceof Object[]) {
            xml.array {
                value.each { subv ->
                    doValue(xml, subv)
                }
            }
        } else throw new InvalidUserDataException("unknown type for plist: "+value)
    }
}
