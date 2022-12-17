/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

@file:Suppress("unused")

package com.ingonoka.usbmanager

import com.ingonoka.cba9driver.ICba9
import com.ingonoka.cba9driver.util.combineAllMessages
import com.ingonoka.usbmanager.Cba9AttachmentEvent.*
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter


//sealed class DeviceAttachmentEvent(
//    val iUsbDeviceAdapter: IUsbDeviceAdapter = IUsbDeviceAdapter.nullAdapter,
//    val time: Instant = Instant.DISTANT_PAST
//) {
//
//    protected fun toString(prefix: String): String {
//        val timeStr = time.toLocalDateTime(TimeZone.of("+08:00"))
//            .toJavaLocalDateTime()
//            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm:ss"))
//        return "$prefix. ${iUsbDeviceAdapter.preferredName}, $timeStr"
//    }
//
//    /**
//     * "Null" event.
//     */
//    object Empty : DeviceAttachmentEvent() {
//        override fun toString(): String = toString("Initial state.")
//    }
//
//    /**
//     * Attachment event of a USB device.
//     */
//    class Attached(device: IUsbDeviceAdapter, time: Instant) : DeviceAttachmentEvent(device, time) {
//        override fun toString(): String = toString("Attached")
//    }
//
//    /**
//     * Detachment event of a USB device.
//     */
//    class Detached(device: IUsbDeviceAdapter, time: Instant) : DeviceAttachmentEvent(device, time) {
//        override fun toString(): String = toString("Detached")
//    }
//
//    /**
//     * Failed event of a USB device.
//     */
//    class Failed(device: IUsbDeviceAdapter, time: Instant) : DeviceAttachmentEvent(device, time) {
//        override fun toString(): String = toString("Failed")
//    }
//}

/**
 * CBA9 attachment and detachment events. If event is [Attached] then the CBA9 was plugged in and is linked to a device driver.
 * If [Detached], then the device was attached, but has been unplugged and unlinked from device driver. The initial
 * status is [Empty], which means even devices for which a driver exists have an unknown attachment status. If something goes wrong
 * in the attachment or detachment process, then a [Failed] status will be used.
 *
 * @property time The time at which the driver was attached, detached, or when it failed.
 */
sealed class Cba9AttachmentEvent(val time: Instant = Clock.System.now()) {

    protected fun toString(time: Instant): String = time.toLocalDateTime(TimeZone.of("+08:00"))
        .toJavaLocalDateTime()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm:ss"))

    /**
     * "Null" event.
     */
    object Empty : Cba9AttachmentEvent() {
        override fun toString(): String = "${toString(time)}, Cba9 No attachment events"
    }

    /**
     * Attachment in progress event.
     */
    class Attaching : Cba9AttachmentEvent() {
        override fun toString(): String = "${toString(time)}, Cba9 attaching."
    }

    /**
     * Attachment event of a [ICba9].
     */
    class Attached(val device: ICba9) : Cba9AttachmentEvent() {
        override fun toString(): String = "${toString(time)}, Cba9 attached: ${device.cba9Validator}"
    }

    /**
     * Detachment event of a [ICba9].
     */
    class Detached(val device: ICba9) : Cba9AttachmentEvent() {
        override fun toString(): String = "${toString(time)}, Cba9 detached: ${device.cba9Validator}"
    }

    /**
     * Failed event of a [ICba9].
     */
    class Failed(private val cause: Throwable) : Cba9AttachmentEvent() {
        override fun toString(): String = "${toString(time)}, Cba9 Failed: ${cause.combineAllMessages(", ")}"
    }
}
