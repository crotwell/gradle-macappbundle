A [Gradle](http://www.gradle.org) Plugin to create a Mac OSX .app application and dmg based on the project.

Available at [plugins.gradle.org](https://plugins.gradle.org/plugin/edu.sc.seis.macAppBundle).

**Version 2.2.0 released 5 January 2018.**

Now available via the Gradle Plugin Portal. Please see:
http://plugins.gradle.org/plugin/edu.sc.seis.macAppBundle

To add to a gradle project, add this to build.gradle and run **gradle createApp** or **gradle createDmg**. The first should work on most systems, the second will only work on Mac OSX as it uses hdiutils which is a Mac-only application. Also see the [Wiki](https://github.com/crotwell/gradle-macappbundle/wiki/Intro) for more information.

For gradle 2.1 or later:
```
plugins {
  id "edu.sc.seis.macAppBundle" version "2.2.0"
}
```

