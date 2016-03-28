A [Gradle](http://www.gradle.org) Plugin to create a Mac OSX .app application and dmg based on the project.

**Version 2.1.5 released 10 December 2015.**

Now available via the Gradle Plugin Portal. Please see:
http://plugins.gradle.org/plugin/edu.sc.seis.macAppBundle

To add to a gradle project, add this to build.gradle and run **gradle createApp** or **gradle createDmg**. The first should work on most systems, the second will only work on Mac OSX as it uses hdiutils which is a Mac-only application. Also see the [Wiki](https://github.com/crotwell/gradle-macappbundle/wiki/Intro) for more information.

As of version 1.0.9, the 
groupId and artifactId changed to edu.sc.seis.gradle and macappbundle.

For gradle 2.1 or later:
```
plugins {
  id "edu.sc.seis.macAppBundle" version "2.1.5"
}
```

or for earlier versions of Gradle:

```

apply plugin: 'edu.sc.seis.macAppBundle'

macAppBundle {
    mainClassName = "com.example.myApp.Start"
}

buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.edu.sc.seis:macAppBundle:2.1.5"
  }
}

```
