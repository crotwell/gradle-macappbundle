package edu.sc.seis.macAppBundle

import java.io.File;
import java.util.Objects;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project;


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
           close
           open
           update without registering applications
           delay 2
        end tell
     end tell
"""
    
    /** Whether or not to copy dependency jars (other than the jar that is produced by the 'jar' task).
     * Defaults to true.
     */
    boolean copyDependencyJars = true;

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
        return Objects.hash(
                appStyle,
                creatorCode,
                icon,
                jvmVersion,
                mainClassName,
                bundleIdentifier,
                appOutputDir,
                dmgOutputDir,
                setFileCmd,
                backgroundImage,
                appName,
                volumeName,
                dmgName,
                javaProperties,
                javaExtras,
                bundleExtras,
                bundleExecutable,
                bundleAllowMixedLocalizations,
                highResolutionCapable,
                bundlePackageType,
                bundleInfoDictionaryVersion,
                bundleDevelopmentRegion,
                arguments,
                bundleJRE,
                jreHome,
                certIdentity,
                codeSignCmd,
                codeSignDeep,
                keyChain,
                backgroundScript,
                copyDependencyJars);
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
        return Objects.equals(appStyle, other.appStyle) &&
                Objects.equals(creatorCode, other.creatorCode) &&
                Objects.equals(icon, other.icon) &&
                Objects.equals(jvmVersion, other.jvmVersion) &&
                Objects.equals(mainClassName, other.mainClassName) &&
                Objects.equals(bundleIdentifier, other.bundleIdentifier) &&
                Objects.equals(appOutputDir, other.appOutputDir) &&
                Objects.equals(dmgOutputDir, other.dmgOutputDir) &&
                Objects.equals(setFileCmd, other.setFileCmd) &&
                Objects.equals(backgroundImage, other.backgroundImage) &&
                Objects.equals(appName, other.appName) &&
                Objects.equals(volumeName, other.volumeName) &&
                Objects.equals(dmgName, other.dmgName) &&
                Objects.equals(bundleExecutable, other.bundleExecutable) &&
                Objects.equals(bundleAllowMixedLocalizations, other.bundleAllowMixedLocalizations) &&
                Objects.equals(highResolutionCapable, other.highResolutionCapable) &&
                Objects.equals(bundlePackageType, other.bundlePackageType) &&
                Objects.equals(bundleInfoDictionaryVersion, other.bundleInfoDictionaryVersion) &&
                Objects.equals(bundleDevelopmentRegion, other.bundleDevelopmentRegion) &&
                Objects.equals(bundleJRE, other.bundleJRE) &&
                Objects.equals(jreHome, other.jreHome) &&
                Objects.equals(certIdentity, other.certIdentity) &&
                Objects.equals(codeSignCmd, other.codeSignCmd) &&
                Objects.equals(codeSignDeep, other.codeSignDeep) &&
                Objects.equals(keyChain, other.keyChain) &&
                Objects.equals(javaProperties, other.javaProperties) &&
                Objects.equals(javaExtras, other.javaExtras) &&
                Objects.equals(bundleExtras, other.bundleExtras) &&
                Objects.equals(arguments, other.arguments) &&
                Objects.equals(backgroundScript, other.backgroundScript) &&
                Objects.equals(copyDependencyJars, other.copyDependencyJars) ;
    }

    
}
