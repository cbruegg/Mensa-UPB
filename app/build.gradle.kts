import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import org.gradle.internal.impldep.com.amazonaws.PredefinedClientConfigurations.defaultConfig
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt
import java.io.FileInputStream
import java.util.Properties

apply(plugin = "kotlin-allopen")
apply(plugin = "kotlinx-serialization")

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "com.cbruegg.mensaupb"
        minSdkVersion(16)
        targetSdkVersion(28)
        versionCode = 18
        versionName = "1.5.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        val apiIdProps = Properties()
        apiIdProps.load(FileInputStream(rootProject.file("api_id.properties")))
        buildConfigField("String", "API_ID", "\"${apiIdProps["id"]}\"")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    dataBinding {
        isEnabled = true
    }

    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }

}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("com.google.android.material:material:1.1.0-alpha01")
    implementation("androidx.preference:preference:1.1.0-alpha01")
    implementation("androidx.browser:browser:1.0.0")
    implementation("androidx.annotation:annotation:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-alpha2")
    implementation("androidx.multidex:multidex:2.0.0")
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation("com.squareup.okhttp3:okhttp:3.12.0")

    implementation("com.firebase:firebase-jobdispatcher:0.8.5")

    implementation("com.cbruegg.mensaupbservice-common:mensaupbservice-common:1.1.0")

    val arrowVersion = "0.8.1"
    implementation("io.arrow-kt:arrow-core:$arrowVersion")

    val daggerVersion = "2.19"
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    implementation("com.google.dagger:dagger:$daggerVersion")
    compileOnly("javax.annotation:jsr250-api:1.0")

    val coroutineVersion = "1.0.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.9.0")

    val requeryVersion = "1.5.1"
    implementation("io.requery:requery:$requeryVersion")
    implementation("io.requery:requery-android:$requeryVersion")
    implementation("io.requery:requery-kotlin:$requeryVersion")
    kapt("io.requery:requery-processor:$requeryVersion")

    val glideVersion = "4.8.0"
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("com.github.bumptech.glide:okhttp3-integration:$glideVersion")
    implementation("com.github.bumptech.glide:recyclerview-integration:$glideVersion") {
        setTransitive(false)
    }
    kapt("com.github.bumptech.glide:compiler:$glideVersion")

    implementation("org.ccil.cowan.tagsoup:tagsoup:1.2.1")

    val stethoVersion = "1.5.0"
    debugImplementation("com.facebook.stetho:stetho:$stethoVersion")
    debugImplementation("com.facebook.stetho:stetho-okhttp3:$stethoVersion")

    val aarchVersion = "2.0.0"
    implementation("androidx.lifecycle:lifecycle-runtime:$aarchVersion")
    implementation("androidx.lifecycle:lifecycle-extensions:$aarchVersion")
    kapt("androidx.lifecycle:lifecycle-compiler:$aarchVersion")

    androidTestImplementation("org.jetbrains.kotlin:kotlin-test:${KotlinCompilerVersion.VERSION}")
    androidTestImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.1.0")
    androidTestImplementation("androidx.test:rules:1.1.0")
    androidTestImplementation("androidx.annotation:annotation:1.0.0")
    androidTestImplementation("org.mockito:mockito-android:2.23.4")
}

buildscript {
    val kotlinVersion = project.extra["kotlinVersion"]
    repositories {
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    }
}
repositories {
    google()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://dl.bintray.com/cbruegg/cbruegg")
}
