/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

rootProject.name = "cba9-driver"
include(":lib", ":cba9driverdemo")

pluginManagement {
    val asciidoctorVersion: String by settings
    val dokkaVersion: String by settings

    plugins {
        id("org.asciidoctor.jvm.convert") version asciidoctorVersion
        id("org.jetbrains.dokka") version dokkaVersion

    }

}
