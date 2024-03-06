/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

repositories {
    google()
    mavenCentral()
}

android {
    namespace = "com.ingonoka.cba9driverdemo"

    compileSdk = project.properties["android_compile_sdk_version"].toString().toInt()
//    buildToolsVersion android_build_tools_version

    defaultConfig {

        minSdk = project.properties["android_min_sdk_version"].toString().toInt()
        targetSdk = project.properties["android_target_sdk_version"].toString().toInt()
        versionCode = project.properties["versionCode"].toString().toInt()
        versionName = project.version.toString()
        resourceConfigurations.addAll(listOf("en","de"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility =   JavaVersion.VERSION_1_8
        targetCompatibility =  JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:${providers.gradleProperty("androidx_core_version").get()}")
    implementation("androidx.appcompat:appcompat:${providers.gradleProperty("androidx_appcompat_version").get()}")
    implementation("com.google.android.material:material:${providers.gradleProperty("android_material_version").get()}")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${providers.gradleProperty("androidx_lifecycle_version").get()}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${providers.gradleProperty("androidx_lifecycle_version").get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${providers.gradleProperty("androidx_lifecycle_version").get()}")
    implementation("androidx.navigation:navigation-fragment-ktx:${providers.gradleProperty("androidx_navigation_version").get()}")
    implementation("androidx.navigation:navigation-ui-ktx:${providers.gradleProperty("androidx_navigation_version").get()}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${providers.gradleProperty("coroutines_version").get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${providers.gradleProperty("coroutines_version").get()}")

    implementation("org.slf4j:slf4j-api:${providers.gradleProperty("slf4j_version").get()}")
    implementation("com.github.tony19:logback-android:${providers.gradleProperty("logback_android_version").get()}")
    implementation("com.ingonoka:cba9driver:v0.3-8-g9518e52")

//    implementation(project(":lib"))
}