import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.io.FileInputStream
import java.util.Properties

apply(plugin = "kotlin-allopen")
apply(plugin = "kotlinx-serialization")
apply(plugin = "io.fabric")
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        applicationId = "com.cbruegg.mensaupb"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 25
        versionName = "1.6.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        val apiIdProps = Properties()
        apiIdProps.load(FileInputStream(rootProject.file("api_id.properties")))
        buildConfigField("String", "API_ID", "\"${apiIdProps["id"]}\"")
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"https://cbruegg.com/mensa-upb-privacy-policy/\"")
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

    dataBinding {
        isEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions {
        freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
        allWarningsAsErrors = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("com.google.android.material:material:1.1.0-alpha07")
    implementation("androidx.preference:preference:1.1.0-beta01")
    implementation("androidx.browser:browser:1.0.0")
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-beta1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation("com.squareup.okhttp3:okhttp:3.14.2")
    implementation("androidx.core:core-ktx:1.0.2")
    implementation("androidx.fragment:fragment-ktx:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.0.0")

    implementation("com.cbruegg.mensaupbservice-common:mensaupbservice-common:1.1.1")

    val arrowVersion = "0.8.1"
    implementation("io.arrow-kt:arrow-core:$arrowVersion")

    val daggerVersion = "2.23.1"
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    implementation("com.google.dagger:dagger:$daggerVersion")
    compileOnly("javax.annotation:jsr250-api:1.0")

    val coroutineVersion = "1.2.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.11.0")

    val requeryVersion = "1.6.1"
    implementation("io.requery:requery:$requeryVersion")
    implementation("io.requery:requery-android:$requeryVersion")
    implementation("io.requery:requery-kotlin:$requeryVersion")
    kapt("io.requery:requery-processor:$requeryVersion")

    val glideVersion = "4.9.0"
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("com.github.bumptech.glide:okhttp3-integration:$glideVersion")
    implementation("com.github.bumptech.glide:recyclerview-integration:$glideVersion") {
        setTransitive(false)
    }
    kapt("com.github.bumptech.glide:compiler:$glideVersion")

    implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")

    implementation("com.crashlytics.sdk.android:crashlytics:2.10.1")

    val stethoVersion = "1.5.1"
    debugImplementation("com.facebook.stetho:stetho:$stethoVersion")
    debugImplementation("com.facebook.stetho:stetho-okhttp3:$stethoVersion")

    val aarchVersion = "2.0.0"
    implementation("androidx.lifecycle:lifecycle-runtime:$aarchVersion")
    implementation("androidx.lifecycle:lifecycle-extensions:$aarchVersion")
    kapt("androidx.lifecycle:lifecycle-compiler:$aarchVersion")

    implementation("androidx.work:work-runtime-ktx:2.0.1")

    androidTestImplementation("org.jetbrains.kotlin:kotlin-test:${KotlinCompilerVersion.VERSION}")
    androidTestImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.annotation:annotation:1.1.0")
    androidTestImplementation("org.mockito:mockito-android:2.28.2")
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
apply(plugin = "com.google.gms.google-services")