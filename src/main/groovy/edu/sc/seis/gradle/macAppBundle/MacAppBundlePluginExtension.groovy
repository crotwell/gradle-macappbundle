package edu.sc.seis.gradle.macAppBundle

import java.io.File;

import org.gradle.api.Project;


class MacAppBundlePluginExtension implements Serializable {

    /** configures default values that depend on values set in the build file like version, and so must be
     * done late in the run order, after the build script is evaluated but before any task in the plugin is run
     * @param project
     */
    void configureDefaults(Project project) {
        if (appName == null) appName = "${->project.name}"
        if (volumeName == null) volumeName = "${->project.name}-${->project.version}"
        if (dmgName == null) dmgName = "${->project.name}-${->project.version}"
        if (jvmVersion == null) jvmVersion = project.targetCompatibility.toString()+"+"
    }
    
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
    
    /** Icon for this application, probably needs to be a '.icns' file. Defaults to the Apple GenericApp.icns. */
    String icon = 'GenericApp.icns'
    
    /** The JVM version needed. Can append a + to set a minimum. */
    String jvmVersion
    
    /** The background image for the DMG. */
    String backgroundImage
    
    /** The name of the application, without the .app extension */
    String appName
    
    /** The name of the volume */
    String volumeName
    
    /** The base name of the dmg file, without the .dmg extension. */
    String dmgName
    
    /** Should the app use the Mac default of a single screen menubar (true) or a menubar per window (false). 
     * Default is true.
     */
    boolean useScreenMenuBar = true
    
    /** The name of the executable run by the bundle.
     * Default is 'JavaApplicationStub'.
     */
    String bundleExecutable = 'JavaApplicationStub'
    
    /** BundleAllowMixedLocalizations, default is true */
    boolean bundleAllowMixedLocalizations = true
    
    /** undlePackageType, default is 'APPL' */
    String bundlePackageType = 'APPL'
    
    /** BundleInfoDictionaryVersion, default is '6.0' */
    String bundleInfoDictionaryVersion = '6.0'
    
    /** The development region.
     * Default is 'English'.
     */
    String bundleDevelopmentRegion = 'English'
    
    /** Any extra xml that should be included in the info.plist file. Will be added
     *  to the bottom inside the outermost <dict> element.
     */
    String extras = ""
    
    /** for codesign */
    String certIdentity
    
    /** for codesign */
    String codeSignCmd = "codesign"
    
    public File getPlistFileForProject(Project project) {
        return project.file("${project.buildDir}/${outputDir}/${appName}.app/Contents/Info.plist")
    }
    
    public File getPkgInfoFileForProject(Project project) {
        return project.file("${project.buildDir}/${outputDir}/${appName}.app/Contents/PkgInfo")
    }
    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((creatorCode == null) ? 0 : creatorCode.hashCode());
        result = prime * result + ((icon == null) ? 0 : icon.hashCode());
        result = prime * result + ((jvmVersion == null) ? 0 : jvmVersion.hashCode());
        result = prime * result + ((mainClassName == null) ? 0 : mainClassName.hashCode());
        result = prime * result + ((outputDir == null) ? 0 : outputDir.hashCode());
        result = prime * result + ((setFileCmd == null) ? 0 : setFileCmd.hashCode());
        result = prime * result + ((backgroundImage == null) ? 0 : backgroundImage.hashCode());
        result = prime * result + ((appName == null) ? 0 : appName.hashCode());
        result = prime * result + ((volumeName == null) ? 0 : volumeName.hashCode());
        result = prime * result + ((dmgName == null) ? 0 : dmgName.hashCode());
        result = prime * result + (useScreenMenuBar ? 1231 : 1237);
        result = prime * result + ((bundleExecutable == null) ? 0 : bundleExecutable.hashCode());
        result = prime * result + (bundleAllowMixedLocalizations ? 1231 : 1237);
        result = prime * result + ((bundlePackageType == null) ? 0 : bundlePackageType.hashCode());
        result = prime * result + ((bundleInfoDictionaryVersion == null) ? 0 : bundleInfoDictionaryVersion.hashCode());
        result = prime * result + ((bundleDevelopmentRegion == null) ? 0 : bundleDevelopmentRegion.hashCode());
        result = prime * result + ((extras == null) ? 0 : extras.hashCode());
        result = prime * result + ((certIdentity == null) ? 0 : certIdentity.hashCode());
        result = prime * result + ((codeSignCmd == null) ? 0 : codeSignCmd.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this.is(obj))
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MacAppBundlePluginExtension other = (MacAppBundlePluginExtension)obj;
        if (creatorCode == null) {
            if (other.creatorCode != null)
                return false;
        } else if (!creatorCode.equals(other.creatorCode))
            return false;
        if (icon == null) {
            if (other.icon != null)
                return false;
        } else if (!icon.equals(other.icon))
            return false;
        if (jvmVersion == null) {
            if (other.jvmVersion != null)
                return false;
        } else if (!jvmVersion.equals(other.jvmVersion))
            return false;
        if (mainClassName == null) {
            if (other.mainClassName != null)
                return false;
        } else if (!mainClassName.equals(other.mainClassName))
            return false;
        if (outputDir == null) {
            if (other.outputDir != null)
                return false;
        } else if (!outputDir.equals(other.outputDir))
            return false;
        if (setFileCmd == null) {
            if (other.setFileCmd != null)
                return false;
        } else if (!setFileCmd.equals(other.setFileCmd))
            return false;
        if (backgroundImage == null) {
            if (other.backgroundImage != null)
                return false;
        } else if (!backgroundImage.equals(other.backgroundImage))
            return false;
        if (appName == null) {
            if (other.appName != null)
                return false;
        } else if (!appName.equals(other.appName))
            return false;
        if (volumeName == null) {
            if (other.volumeName != null)
                return false;
        } else if (!volumeName.equals(other.volumeName))
            return false;
        if (dmgName == null) {
            if (other.dmgName != null)
                return false;
        } else if (!dmgName.equals(other.dmgName))
            return false;
        if (bundleExecutable == null) {
            if (other.bundleExecutable != null)
                return false;
        } else if (!bundleExecutable.equals(other.bundleExecutable))
            return false;
        if (bundleAllowMixedLocalizations != other.bundleAllowMixedLocalizations)
            return false;
        if (bundlePackageType == null) {
            if (other.bundlePackageType != null)
                return false;
        } else if (!bundlePackageType.equals(other.bundlePackageType))
            return false;
        if (bundleInfoDictionaryVersion == null) {
            if (other.bundleInfoDictionaryVersion != null)
                return false;
        } else if (!bundleInfoDictionaryVersion.equals(other.bundleInfoDictionaryVersion))
            return false;
        if (bundleDevelopmentRegion == null) {
            if (other.bundleDevelopmentRegion != null)
                return false;
        } else if (!bundleDevelopmentRegion.equals(other.bundleDevelopmentRegion))
            return false;
        if (certIdentity == null) {
            if (other.certIdentity != null)
                return false;
        } else if (!certIdentity.equals(other.certIdentity))
            return false;
        if (codeSignCmd == null) {
            if (other.codeSignCmd != null)
                return false;
        } else if (!codeSignCmd.equals(other.codeSignCmd))
            return false;
        if (useScreenMenuBar != other.useScreenMenuBar)
            return false;
        if (extras == null) {
            if (other.extras != null)
                return false;
        } else if (!extras.equals(other.extras))
            return false;
        return true;
    }
    
    
}
