import org.jetbrains.kotlin.config.KotlinCompilerVersion

apply(plugin = "com.github.ben-manes.versions")

allprojects {
    extra["kotlinVersion"] = KotlinCompilerVersion.VERSION
    repositories {
        google()
        jcenter()
        maven("https://www.jitpack.io")
    }
}

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.github.ben-manes:gradle-versions-plugin:0.36.0")
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath(kotlin("gradle-plugin", "1.4.10"))
        classpath("com.google.gms:google-services:4.3.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.4.1")
    }
}

