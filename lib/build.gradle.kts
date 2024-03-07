@file:Suppress("UnstableApiUsage")

/*
* Copyright (c) 2023. Ingo Noka
* This file belongs to project cba9-driver.
* This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
* To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
* Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
*
*/

plugins {
    id("com.android.library")
    id("kotlin-android")
//    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("maven-publish")
    id("signing")
    id("org.asciidoctor.jvm.convert")
    id("org.jetbrains.dokka")
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

android {
    namespace = "com.ingonoka"

    compileSdk = project.properties["android_compile_sdk_version"].toString().toInt()

    defaultConfig {

        minSdk = project.properties["android_min_sdk_version"].toString().toInt()
        resourceConfigurations.addAll(listOf("en", "de"))

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
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${providers.gradleProperty("coroutines_version").get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${providers.gradleProperty("coroutines_version").get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${providers.gradleProperty("datetime_version").get()}")

    implementation("org.slf4j:slf4j-api:${providers.gradleProperty("slf4j_version").get()}")

    implementation("androidx.room:room-ktx:${providers.gradleProperty("room_version").get()}")
//    kapt("androidx.room:room-compiler:${providers.gradleProperty("room_version").get()}")
    ksp("androidx.room:room-compiler:${providers.gradleProperty("room_version").get()}")

}

// See configuration on ~/.gradle/gradle.properties
signing {
    sign(publishing.publications)
}

afterEvaluate {
    publishing {
        publications {

            register<MavenPublication>("release") {
                groupId = "com.ingonoka"
                artifactId = "cba9driver"
                version = version.toString()

                from(components["release"])
            }

            register<MavenPublication>("debug") {
                groupId = "com.ingonoka"
                artifactId = "cba9driver-debug"
                version = version.toString()

                from(components["debug"])

            }

            withType<MavenPublication> {
                pom {
                    name.set("CBA9 Banknote Acceptor Driver Library")
                    description.set("A library implementing the SSP protocol for CBA9 Banknote Acceptors")
                    url.set("https://github.com/ingonoka/cba9-driver")

                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/ingonoka/cba9-driver/issues")
                    }

                    licenses {
                        license {
                            name.set(project.properties["pom_licenseName"].toString())
                            url.set(project.properties["pom_licenseUrl"].toString())
                        }
                    }
                    scm {
                        connection.set("scm:git:git@github.com:ingonoka/cba9-driver")
                        developerConnection.set("scm:git:git@github.com:ingonoka/cba9-driver")
                        url.set("https://github.com/ingonoka/cba9-driver")
                    }
                    developers { developer { name.set(project.properties["pom_developer"].toString()) } }
                }
            }

            repositories {

                maven {
                    name = "Sonatype"
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = providers.gradleProperty("sonatype_uid").get()
                        password = providers.gradleProperty("sonatype_pw").get()
                    }
                }

                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/ingonoka/cba9-driver")
                    credentials {
                        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
                    }
                }
            }
        }
    }
}

tasks.dokkaHtmlPartial.configure {
    outputDirectory.set(layout.buildDirectory.file("dokka").get().asFile)
    moduleName.set("NFC Adapter for Android")
    dokkaSourceSets {
        configureEach {
            includes.from("module.md")
        }
    }
}