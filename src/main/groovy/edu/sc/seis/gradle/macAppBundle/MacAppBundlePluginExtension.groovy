package edu.sc.seis.gradle.macAppBundle

import java.io.File;

import org.gradle.api.Project;


class MacAppBundlePluginExtension {

    String setFileCmd = "/Developer/Tools/SetFile"
    
    String outputDir = "macApp"

    String mainClassName
    
    String creatorCode = '????'
    
    String icon = 'GenericApp.icns'
    
    String jvmVersion = '1.5+' // should get this from Java plugin...
    
    boolean useScreenMenuBar = true
    
    public File getPlistFileForProject(Project project) {
        return project.file("${project.buildDir}/${outputDir}/${project.name}.app/Contents/Info.plist")
    }
    
    public File getPkgInfoFileForProject(Project project) {
        return project.file("${project.buildDir}/${outputDir}/${project.name}.app/Contents/PkgInfo")
    }
    
    public void initExtensionDefaults(Project project) {
        
    }
}
