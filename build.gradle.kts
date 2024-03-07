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
        classpath("org.asciidoctor:asciidoctor-gradle-jvm:4.0.2")//${project.properties["asciidoctor_version"].toString()}")
    }
}


plugins {

    kotlin("jvm") version "1.9.10" apply false
    id("org.asciidoctor.jvm.convert")
    id("org.jetbrains.dokka")
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}

/**
 * Number of commits from the start of this repository
 */
fun getVersionCode(): Int = try {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine =
            listOf("git", "rev-list", "--first-parent", "--count", "master")
        standardOutput = stdout
    }
    Integer.parseInt(stdout.toString().trim())
} catch (e: Exception) {
    -1
}

/**
 * Get a version name of the form "v0.3-8-g9518e52", which is the tag
 * assigned to the commit (v0.3), the number of commits since the
 * commit the tag is assigned to and the hash of the latest commit
 */
fun getVersionName(): String = try {
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
    set(
        "pom_licenseName",
        "Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)"
    )
    set(
        "pom_licenseUrl",
        "https://creativecommons.org/licenses/by-nc-nd/4.0/"
    )
    set("group", "com.ingonoka")
    set("versionName", getVersionName())
    set("versionCode", getVersionCode())
}

group = "com.ingonoka"


tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(layout.buildDirectory.file("dokka").get().asFile)
    moduleName.set("CBA9 Driver for Android")
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
    }
}