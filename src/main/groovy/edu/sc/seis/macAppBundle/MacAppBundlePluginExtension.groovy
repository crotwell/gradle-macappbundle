package edu.sc.seis.macAppBundle

import java.io.File;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;


class MacAppBundlePluginExtension implements Serializable {

    /** configures default values that depend on values set in the build file like version, and so must be
     * done late in the run order, after the build script is evaluated but before any task in the plugin is run
     * @param project
     */
    void configureDefaults(Project project) {
        if (appName == null) appName = "${->project.name}"
        if (bundleIdentifier == null) bundleIdentifier = "${->mainClassName}"
        if (volumeName == null) volumeName = "${->project.name}-${->project.version}"
        if (dmgName == null) dmgName = "${->project.name}-${->project.version}"
        if (jvmVersion == null) jvmVersion = project.targetCompatibility.toString()+"+"
        if (dmgOutputDir == null) dmgOutputDir = "${->project.distsDirName}"
        if (jarTask == null) jarTask = JavaPlugin.JAR_TASK_NAME
        setAppStyle(appStyle)
    }
    
    /** The style of .app created. Use 'Apple' for the original Apple Java in OSX 10.8 and earlier. Starting in
    OSX 10.9 there can be either Apple Java (1.6) or Oracle Java (1.7) and the internals of the Info.plist and
    the executable stub are different. Setting this will also change the 
    bundleExecutable and the jarSubdir as both of these are different in Oracle versus Apple styles.
    The default is 'Oracle'.
    
    More information on the new Oracle style .app can be found <a href="https://java.net/projects/appbundler">here</a>.
    */
    String appStyle = 'Oracle'
    
    def setAppStyle(String val) {
        appStyle = val
        if (val == 'Oracle') {
            bundleExecutable = 'JavaAppLauncher'
            jarSubdir = 'Java'
        } else if (val == 'Apple') {
            bundleExecutable = 'JavaApplicationStub'
            jarSubdir = 'Resources/Java'
        } else {
            throw new InvalidUserDataException("I don't understand appStyle='${appStyle}', should be one of 'Apple' or 'Oracle'")
        }
    }
    
    /** The command SetFile, usually located in /usr/bin, but might be in /Developer/Tools,
     *  that sets the magic bit on a .app directory to turn it into a OSX Application.
     *  This does not seem to be required to generate a recognizable .app application.
     */
    String setFileCmd = "/usr/bin/SetFile"
    
    /** The output directory for building the app, relative to the build directory. */
    String appOutputDir = "macApp"
    
    /** The output directory for building the dmg, relative to the build directory. */
    String dmgOutputDir

    /** The initial class to start the application, must contain a public static void main method. */ 
    String mainClassName

    /** The configuration used to copy jars into the app. Default is runtime, which means use the runtime configuration
        from the java plugin. */ 
    String runtimeConfigurationName = "runtime"
    
    /** The task that generates the jar, defaults to JavaPlugin.JAR_TASK_NAME. */
    String jarTask
    
    /** The CFBundleIdentifier, defaults to mainClassName. */
    String bundleIdentifier
    
    /** Creator code, issued by Apple. Four question marks is the default if no code has been issued. */
    String creatorCode = '????'
    
    /** Icon for this application, probably needs to be a '.icns' file. Defaults to the Apple GenericApp.icns. */
    String icon = 'GenericApp.icns'
    
    /** The JVM version needed. Can append a + to set a minimum. */
    String jvmVersion
    
    /** The background image for the DMG. */
    String backgroundImage
    
    /** The name of the application, without the .app extension. 
     * Defaults to project.name */
    String appName
    
    /** The name of the volume. Defaults to project.name-project.version */
    String volumeName
    
    /** The base name of the dmg file, without the .dmg extension. 
     * Defaults to project.name-project.version*/
    String dmgName
    
    /** Map of properties to be put as -D options for Oracle Java an 
     * in the Properties dict inside the Java dict for Apple. Usage should be like
        javaProperties.put("apple.laf.useScreenMenuBar", "true") */
    Map javaProperties = [:]
    
    /** Map of extra java key-value pairs to be put in JVMOptions for Oracle and 
     * put in the java level dict inside Info.plist for Apple. Usage should be like
        javaExtras.put("mykey", "myvalue") */
    Map javaExtras = [:]
    
    /** Map of extra bundle key-value pairs to be put in the top level dict inside Info.plist. Usage should be like
        bundleExtras.put("mykey", "myvalue") */
    Map bundleExtras = [:]
    
    /** List of arguments to pass to the application. Only used for Oracle-style apps. */
    List arguments = []
    
    /** subdir of the Contents dir to put the jar files. Defaults to Java for Oracle and
     * to Resources/Java for Apple.
     */
    String jarSubdir = 'Java'
    
    /** The name of the executable run by the bundle.
     * Default is 'JavaAppLauncher'. This is also set when setting the style to Oracle or Apple.
     */
    String bundleExecutable = 'JavaAppLauncher'
    
    /** BundleAllowMixedLocalizations, default is true */
    boolean bundleAllowMixedLocalizations = true
    
    /** NSHighResolutionCapable for retina display, default is true */
    boolean highResolutionCapable = true
    
    /** BundlePackageType, default is 'APPL' */
    String bundlePackageType = 'APPL'
    
    /** BundleInfoDictionaryVersion, default is '6.0' */
    String bundleInfoDictionaryVersion = '6.0'
    
    /** The development region.
     * Default is 'English'.
     */
    String bundleDevelopmentRegion = 'English'
    
    /** Whether or not to bundle the JRE in the .app. Only used if the app style is Oracle.
     * Defaults to false.
     */
    boolean bundleJRE = false;
    
    /** Directory from which to copy the JRE. Generally this will be the same as
    $JAVA_HOME or the result of /usr/libexec/java_home. Note that to be compatible
    with the appbundler utility from Oracle, this is usually the Contents/Home
    subdirectory of the JDK install.
    
    If bundleJRE is true, but jreHome is null, it will be set to the output of
    /usr/libexec/java_home, which should be correct in most cases.
    
    For example:
    /Library/Java/JavaVirtualMachines/jdk1.7.0_51.jdk/Contents/Home
    */
    String jreHome
    
    /** for codesign */
    String certIdentity = null
    
    /** for codesign */
    String codeSignCmd = "codesign"
    
    /** for codesign */
    String keyChain = null
    
    /** codesign use --deep */
    boolean codeSignDeep = true;
    
    String appIconX = "160", appIconY = "205";
    String appFolderX = "360", appFolderY = "205";
    
    // will be set by using sips when copying image to .background folder
    String backgroundImageWidth = "100";
    String backgroundImageHeight = "100";
    
    /** An AppleScript script for setting the background image of the dmg.
     * see 
     * http://asmaloney.com/2013/07/howto/packaging-a-mac-os-x-application-using-a-dmg/
     */
    String backgroundScript = """
   tell application "Finder"
      tell disk "\${VOL_NAME}"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to { 0, 0, \${IMAGE_WIDTH}, \${IMAGE_HEIGHT} }
           set the position of the container window to {400, 100}
           set viewOptions to the icon view options of container window
           set arrangement of viewOptions to not arranged
           set icon size of viewOptions to 72
           set background picture of viewOptions to file ".background:\${DMG_BACKGROUND_IMG}"
           set position of item "\${APP_NAME}.app" of container window to { \${APPICONX}, \${APPICONY} }
           set position of item "Applications" of container window to { \${APPFOLDERX}, \${APPFOLDERY} }
           set position of item ".background" of container window to { 100, 5000 }
           if exists item ".fseventsd"
              set position of item ".fseventsd" of container window to { 120, 5000 }
           end
           if exists item ".DS_Store"
              set position of item ".DS_Store" of container window to { 140, 5000 }
           end
           if exists item ".Trashes"
              set position of item ".Trashes" of container window to { 160, 5000 }
           end
           close
           open
           update without registering applications
           delay 2
      end tell
   end tell
"""
    
    String getJreHome() {
        // ensure jreHome is set, finding it if needed, before running task
        if (jreHome == null && Os.isFamily(Os.FAMILY_MAC) && appStyle == 'Oracle') {
            String javaHomeCommand = """/usr/libexec/java_home"""// Create the String
            File jhFile = new File((String)javaHomeCommand);
            if ( ! jhFile.exists()) {
                throw new RuntimeException("jreHome not set and unable to find "+javaHomeCommand+", is oracle java installed?");
            }
            def proc = javaHomeCommand.execute() // Call *execute* on the string
            proc.waitFor()                       // Wait for the command to finish

            // Obtain status and output
            def retCode = proc.exitValue();
            if (retCode == 0) {
                // *out* from the external program is *in* for groovy
                return proc.in.text.trim();
            } else {
                throw new RuntimeException("jreHome not set and return code of "+javaHomeCommand+" is nonzero: "+retCode);
            }
        }
        return jreHome;
    }

    public String getJREDirName() {
        return new File(getJreHome()).getParentFile().getParentFile().getName()
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appStyle == null) ? 0 : appStyle.hashCode());
        result = prime * result + ((creatorCode == null) ? 0 : creatorCode.hashCode());
        result = prime * result + ((jarTask == null) ? 0 : jarTask.hashCode());
        result = prime * result + ((runtimeConfiguration == null) ? 0 : runtimeConfigurationName.hashCode());
        result = prime * result + ((icon == null) ? 0 : icon.hashCode());
        result = prime * result + ((jvmVersion == null) ? 0 : jvmVersion.hashCode());
        result = prime * result + ((mainClassName == null) ? 0 : mainClassName.hashCode());
        result = prime * result + ((bundleIdentifier == null) ? 0 : bundleIdentifier.hashCode());
        result = prime * result + ((appOutputDir == null) ? 0 : appOutputDir.hashCode());
        result = prime * result + ((dmgOutputDir == null) ? 0 : dmgOutputDir.hashCode());
        result = prime * result + ((setFileCmd == null) ? 0 : setFileCmd.hashCode());
        result = prime * result + ((backgroundImage == null) ? 0 : backgroundImage.hashCode());
        result = prime * result + ((appName == null) ? 0 : appName.hashCode());
        result = prime * result + ((volumeName == null) ? 0 : volumeName.hashCode());
        result = prime * result + ((dmgName == null) ? 0 : dmgName.hashCode());
        result = prime * result + ((javaProperties == null || javaProperties.isEmpty()) ? 0 : javaProperties.hashCode());
        result = prime * result + ((javaExtras == null) ? 0 : javaExtras.hashCode());
        result = prime * result + ((bundleExtras == null) ? 0 : bundleExtras.hashCode());
        result = prime * result + ((bundleExecutable == null) ? 0 : bundleExecutable.hashCode());
        result = prime * result + (bundleAllowMixedLocalizations ? 1231 : 1237);
        result = prime * result + (highResolutionCapable ? 1231 : 1237);
        result = prime * result + ((bundlePackageType == null) ? 0 : bundlePackageType.hashCode());
        result = prime * result + ((bundleInfoDictionaryVersion == null) ? 0 : bundleInfoDictionaryVersion.hashCode());
        result = prime * result + ((bundleDevelopmentRegion == null) ? 0 : bundleDevelopmentRegion.hashCode());
        result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result + (bundleJRE ? 1231 : 1237);
        result = prime * result + ((jreHome == null) ? 0 : jreHome.hashCode());
        result = prime * result + ((certIdentity == null) ? 0 : certIdentity.hashCode());
        result = prime * result + ((codeSignCmd == null) ? 0 : codeSignCmd.hashCode());
        result = prime * result + (codeSignDeep ? 1231 : 1237);
        result = prime * result + ((keyChain == null) ? 0 : keyChain.hashCode());
        result = prime * result + ((backgroundScript == null) ? 0 : backgroundScript.hashCode());
        
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
        if (appStyle == null) {
            if (other.appStyle != null)
                return false;
        } else if (!appStyle.equals(other.appStyle))
            return false;
        if (creatorCode == null) {
            if (other.creatorCode != null)
                return false;
        } else if (!creatorCode.equals(other.creatorCode))
            return false;
        if (jarTask == null) {
            if (other.jarTask != null)
                return false;
        } else if (!jarTask.equals(other.jarTask))
            return false;
        if (runtimeConfigurationName == null) {
            if (other.runtimeConfigurationName != null)
                return false;
        } else if (!runtimeConfigurationName.equals(other.runtimeConfigurationName))
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
        if (bundleIdentifier == null) {
            if (other.bundleIdentifier != null)
                return false;
        } else if (!bundleIdentifier.equals(other.bundleIdentifier))
            return false;
        if (appOutputDir == null) {
            if (other.appOutputDir != null)
                return false;
        } else if (!appOutputDir.equals(other.appOutputDir))
            return false;
        if (dmgOutputDir == null) {
            if (other.dmgOutputDir != null)
                return false;
        } else if (!dmgOutputDir.equals(other.dmgOutputDir))
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
        if (highResolutionCapable != other.highResolutionCapable)
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
        if (bundleJRE != other.bundleJRE)
            return false;
        if (jreHome == null) {
            if (other.jreHome != null)
                return false;
        } else if (!jreHome.equals(other.jreHome))
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
        if (codeSignDeep != other.codeSignDeep)
            return false;
        if (keyChain == null) {
            if (other.keyChain != null)
                return false;
        } else if (!keyChain.equals(other.keyChain))
            return false;
            
        if (javaProperties == null) {
            if (other.javaProperties != null)
                return false;
        } else if (!javaProperties.equals(other.javaProperties))
            return false;
        if (javaExtras == null) {
            if (other.javaExtras != null)
                return false;
        } else if (!javaExtras.equals(other.javaExtras))
            return false;
        if (bundleExtras == null) {
            if (other.bundleExtras != null)
                return false;
        } else if (!bundleExtras.equals(other.bundleExtras))
            return false;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (backgroundScript == null) {
            if (other.backgroundScript != null)
                return false;
        } else if (!backgroundScript.equals(other.backgroundScript))
            return false;
            
        return true;
    }
    
    
}
