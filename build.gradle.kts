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
        classpath("com.github.ben-manes:gradle-versions-plugin:0.25.0")
        classpath("com.android.tools.build:gradle:3.6.0-alpha12")
        classpath(kotlin("gradle-plugin", "1.3.50"))
        classpath("com.google.gms:google-services:4.3.2")
        classpath("io.fabric.tools:gradle:1.31.1")
    }
}

