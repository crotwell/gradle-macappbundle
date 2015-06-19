A [Gradle](http://www.gradle.org) Plugin to create a Mac OSX .app application based on the project.

**Version 2.1.1 released 18 June 2015.**

Now available via the Gradle Plugin Portal. Please see:
http://plugins.gradle.org/plugin/edu.sc.seis.macAppBundle

To add to a gradle project, add this to build.gradle and run **gradle createApp** or **gradle createDmg**. The first should work on most systems, the second will only work on Mac OSX as it uses hdiutils which is a Mac-only application. Also see the [Wiki](https://github.com/crotwell/gradle-macappbundle/wiki/Intro) for more information.

As of version 1.0.9, the macappbundle is available from mavenCentral, which fixes an issue with downloads no longer working in Gradle from code.google.com. The groupId and artifactId have also changed to edu.sc.seis.gradle and macappbundle.

For gradle 2.1:
```
plugins {
  id "edu.sc.seis.macAppBundle" version "2.1.1"
}
```

or for earlier versions of Gradle:

```

apply plugin: 'macAppBundle'

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
    classpath "gradle.plugin.edu.sc.seis:macAppBundle:2.1.1"
  }
}

```
