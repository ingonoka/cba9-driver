import org.asciidoctor.gradle.jvm.AsciidoctorTask
import java.io.ByteArrayOutputStream

project.version = getVersionName()

project(":lib") {
    version = getVersionName()
}

buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${project.properties["android_gradle_plugin_version"].toString()}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.properties["kotlin_version"].toString()}")
        classpath("org.asciidoctor:asciidoctor-gradle-jvm:3.3.2")//${project.properties["asciidoctor_version"].toString()}")
    }
}


plugins {

//    id("maven-publish")
    id("org.asciidoctor.jvm.convert")
    id("org.jetbrains.dokka")
//    id("org.jetbrains.kotlin.android") version "${project.properties["kotlin_version"].toString()}"
//    id 'com.android.application' version '7.0.4' apply false
}

fun getVersionCode() = try {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "rev-list", "--first-parent", "--count", "master")
        standardOutput = stdout
    }
    Integer.parseInt(stdout.toString().trim())
} catch (e: Exception) {
    -1
}

fun getVersionName() = try {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "describe", "--tags") //, '--long'
        standardOutput = stdout
    }
    stdout.toString().trim()
} catch (e: Exception) {
    println(e.message)
    "na"
}

extra.apply {
    set("domain", "com.ingonoka")
    set("pom_developer", "Ingo Noka")
    set("pom_licenseName", "Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)")
    set("pom_licenseUrl", "https://creativecommons.org/licenses/by-nc-nd/4.0/")
    set("group","com.ingonoka")
    set("versionName", getVersionName() ?: "na")
    set("versionCode", getVersionCode() ?: 0)
}

group = "com.ingonoka"


tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(buildDir.resolve("$buildDir/dokka"))
    moduleName.set("NFC Adapter for Android")
    includes.from("module.md")
}


asciidoctorj {
    modules {
        diagram.use()
    }
    logLevel = LogLevel.ERROR
}

tasks {
    "asciidoctor"(AsciidoctorTask::class) {
//        baseDirFollowsSourceDir()
//        sourceDir = buildDir.resolve("docs")
//        outputDir.set(buildDir.resolve("build/docs"))
//
//        attributes["source-highlighter"] = "rouge"
//
//        attributes["revnumber"] = getVersionName()
    }
}