/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import android.hardware.usb.UsbConstants

/**
 * Create a printable string for USB class.
 */
fun Int.usbClassStringify(): String {

    @Suppress("KotlinConstantConditions")
    return when (this) {
        UsbConstants.USB_CLASS_APP_SPEC -> "$this (Application specific)"

        UsbConstants.USB_CLASS_AUDIO -> "$this (audio devices)"

        UsbConstants.USB_CLASS_CDC_DATA -> "$this (CDC devices - communications device class)"

        UsbConstants.USB_CLASS_COMM -> "$this (communication devices)"

        UsbConstants.USB_CLASS_CONTENT_SEC -> "$this (content security devices)"

        UsbConstants.USB_CLASS_CSCID -> "$this (content smart card devices)"

        UsbConstants.USB_CLASS_HID -> "$this (human interface devices (for example, mice and keyboards))"

        UsbConstants.USB_CLASS_HUB -> "$this (USB hubs)"

        UsbConstants.USB_CLASS_MASS_STORAGE -> "$this (mass storage devices)"

        UsbConstants.USB_CLASS_MISC -> "$this (wireless miscellaneous devices)"

        UsbConstants.USB_CLASS_PER_INTERFACE -> "$this (class is determined on a per-interface basis)"

        UsbConstants.USB_CLASS_PHYSICA -> "$this (physical devices)"

        UsbConstants.USB_CLASS_PRINTER -> "$this (printers)"

        UsbConstants.USB_CLASS_STILL_IMAGE -> "$this (still image devices (digital cameras))"

        UsbConstants.USB_CLASS_VENDOR_SPEC -> "$this (Vendor specific)"

        UsbConstants.USB_CLASS_VIDEO -> "$this (video devices)"

        UsbConstants.USB_CLASS_WIRELESS_CONTROLLER -> "$this (wireless controller devices)"

        else -> "$this (Unknown class)"
    }
}