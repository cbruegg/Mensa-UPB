import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

apply(plugin = "kotlin-allopen")
apply(plugin = "com.google.firebase.crashlytics")
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cbruegg.mensaupb"
        minSdk = 21
        targetSdk = 36
        versionCode = 40
        versionName = "1.6.21"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        val apiIdProps = Properties()
        apiIdProps.load(FileInputStream(rootProject.file("api_id.properties")))
        val apiId = apiIdProps["id"] ?: error("API ID is missing!")
        buildConfigField("String", "API_ID", "\"$apiId\"")
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"https://cbruegg.com/mensa-upb-privacy-policy\"")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            manifestPlaceholders["enableCrashReporting"] = "true"
        }

        getByName("debug") {
            manifestPlaceholders["enableCrashReporting"] = "false"
        }
    }
    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    namespace = "com.cbruegg.mensaupb"

}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        optIn.add("kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    implementation("com.cbruegg.mensaupbservice-common:mensaupbservice-common:1.1.2")

    val arrowVersion = "2.1.2"
    implementation("io.arrow-kt:arrow-core:$arrowVersion")

    val daggerVersion = "2.56.2"
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    implementation("com.google.dagger:dagger:$daggerVersion")
    compileOnly("javax.annotation:jsr250-api:1.0")

    val coroutineVersion = "1.10.2"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")

    val requeryVersion = "1.6.0"
    implementation("io.requery:requery:$requeryVersion")
    implementation("io.requery:requery-android:$requeryVersion")
    implementation("io.requery:requery-kotlin:$requeryVersion")
    kapt("io.requery:requery-processor:$requeryVersion")

    val glideVersion = "4.16.0"
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("com.github.bumptech.glide:okhttp3-integration:$glideVersion")
    implementation("com.github.bumptech.glide:recyclerview-integration:$glideVersion") {
        isTransitive = false
    }
    kapt("com.github.bumptech.glide:compiler:$glideVersion")

    implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")

    implementation("com.google.firebase:firebase-crashlytics:19.4.4")

    implementation("androidx.lifecycle:lifecycle-runtime:2.9.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    kapt("androidx.lifecycle:lifecycle-compiler:2.9.1")

    implementation("androidx.work:work-runtime:2.10.2")
    implementation("androidx.work:work-runtime-ktx:2.10.2")

    val moshiVersion = "1.10.0"
    implementation("com.squareup.moshi:moshi-parent:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
}

buildscript {
    val kotlinVersion = project.extra["kotlinVersion"]
    repositories {
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    }
}
repositories {
    google()
    maven("https://dl.bintray.com/cbruegg/cbruegg")
}
apply(plugin = "com.google.gms.google-services")