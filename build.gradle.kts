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
        classpath("com.android.tools.build:gradle:8.11.0")
        classpath(kotlin("gradle-plugin", "2.2.0"))
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.2")
    }
}

