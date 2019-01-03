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
        maven("https://maven.fabric.io/public")
    }

    dependencies {
        classpath("com.github.ben-manes:gradle-versions-plugin:0.20.0")
        classpath("com.android.tools.build:gradle:3.2.1")
        classpath(kotlin("gradle-plugin", "1.3.11"))
        classpath("com.google.gms:google-services:4.2.0")
        classpath("io.fabric.tools:gradle:1.26.1")
    }
}

