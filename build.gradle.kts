import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.github.ben-manes.versions") version "0.47.0"
}

allprojects {
    extra["kotlinVersion"] = KotlinCompilerVersion.VERSION
    repositories {
        google()
        mavenCentral()
        maven("https://www.jitpack.io")
    }
}

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath(kotlin("gradle-plugin", "1.6.21"))
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.8")
    }
}

