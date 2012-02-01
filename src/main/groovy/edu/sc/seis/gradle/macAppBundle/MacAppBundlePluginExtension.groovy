package edu.sc.seis.gradle.macAppBundle

import java.io.File;

import org.gradle.api.Project;


class MacAppBundlePluginExtension {

    /** The command SetFile, usually located in /Devloper/Tools, that sets the magic bit on a .app directory
     * to turn it into a OSX Application.
     */
    String setFileCmd = "/Developer/Tools/SetFile"
    
    /** The output directory for building the app */
    String outputDir = "macApp"

    /** The initial class to start the application, must contain a public static void main method. */ 
    String mainClassName
    
    /** Creator code, issued by Apple. Four question marks is the default if no code has been issued. */
    String creatorCode = '????'
    
    /** Icon for this application, defaults to the Apple GenericApp.icns. */
    String icon = 'GenericApp.icns'
    
    /** The JVM version needed. Can append a + to set a minimum. */
    String jvmVersion
    
    /** Should the app use the Mac default of a single screen menubar (true) or a menubar per window (false). 
     * Default is true.
     */
    boolean useScreenMenuBar = true
    
    public File getPlistFileForProject(Project project) {
        return project.file("${project.buildDir}/${outputDir}/${project.name}.app/Contents/Info.plist")
    }
    
    public File getPkgInfoFileForProject(Project project) {
        return project.file("${project.buildDir}/${outputDir}/${project.name}.app/Contents/PkgInfo")
    }
    
    public void initExtensionDefaults(Project project) {
        jvmVersion = project.targetCompatibility+"+"
    }
}
