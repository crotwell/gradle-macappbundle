package edu.sc.seis.gradle.macAppBundle

import java.io.File;

import org.gradle.api.Project;


class MacAppBundlePluginExtension {

    String outputDir = "macApp"

    String mainClassName
    
    String creatorCode = '????'
    
    String icon = 'GenericApp.icns'
    
    String jvmVersion = '1.5+' // should get this from Java plugin...
    
    boolean useScreenMenuBar = true
    
    public File getPlistFileForProject(Project project) {
        return project.file("${project.buildDir}/${outputDir}/${project.name}.app/Contents/Info.plist")
    }
    
    public void initExtensionDefaults(Project project) {
        
    }
}
