/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

/**
 * Create a printable string describing the USB protocol for logging and debugging purposes
 */
fun Int.usbDeviceProtocolStringify(indent: String = ""): String {

    return "$indent$this (${
    when (this) {
        0 -> "Not device specific"
        0xFF -> "Vendor specific"
        else -> "Unknown protocol"
    }
    }"
}
