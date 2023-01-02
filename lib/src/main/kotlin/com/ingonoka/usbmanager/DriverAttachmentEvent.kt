/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import com.ingonoka.cba9driver.util.combineAllMessages
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter


sealed class DriverAttachmentEvent(
    val driver: UsbDriver? = null,
    val time: Instant = Clock.System.now()
) {

    protected fun toString(prefix: String): String {
        val timeStr = time.toLocalDateTime(TimeZone.of("+08:00"))
            .toJavaLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm:ss"))
        return "$prefix. ${driver?.preferredName ?: "NO DEVICE AVAILABLE"}, $timeStr"
    }

    /**
     * "Null" event.
     */
    object Empty : DriverAttachmentEvent() {
        override fun toString(): String = toString("Initial state.")
    }

    /**
     * Usb device is in the process fo getting attached.
     */
    class Attaching(driver: UsbDriver) : DriverAttachmentEvent(driver) {
        override fun toString(): String = toString("Attaching.")
    }

    /**
     * Attachment event of a USB device.
     */
    class Attached(driver: UsbDriver) : DriverAttachmentEvent(driver) {
        override fun toString(): String = toString("Attached")
    }

    /**
     * Detachment event of a USB device.
     */
    class Detached(driver: UsbDriver) : DriverAttachmentEvent(driver) {
        override fun toString(): String = toString("Detached")
    }

    /**
     * Failed event attachment.
     */
    class Failed(private val cause: Throwable) : DriverAttachmentEvent() {
        override fun toString(): String = "${toString("Failed")}, Cba9 Failed: ${cause.combineAllMessages(", ")}"
    }
}
