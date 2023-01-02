/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface

/**
 * Create a printable string describing the USB interface for logging and debugging purposes
 */
fun UsbInterface.stringify(indent: String = "", oneLine: Boolean = false): String =

    buildString {
        if (oneLine) {
            append("${indent}ID: $id, endpoint count: $endpointCount:\n")
        } else {
            appendLine("${indent}ID:                  $id")
            appendLine("${indent}Class:               ${interfaceClass.usbClassStringify()}")
            appendLine("${indent}Protocol:            ${interfaceProtocol.usbDeviceProtocolStringify()}")
            appendLine("${indent}SubClass:            $interfaceSubclass")
            appendLine("${indent}Number of Endpoints: $endpointCount\n")
        }
        for (endpointIndex in 0 until endpointCount) {
            append("$indent\tEndpoint: $endpointIndex: ")
            appendLine(getEndpoint(endpointIndex).stringify(indent + "\t\t", oneLine = oneLine))
        }
    }

/**
 * Retrieve a receiving endpoint with a particular index, Return null if such an endpoint does not exists
 */
fun UsbInterface.receivingEndpointOrNull(endpointNum: Int) = receivingEndpoints().firstOrNull { usbEndpoint ->
    usbEndpoint.endpointNumber == endpointNum
}

/**
 * Retrieve a sending endpoint with a particular index, Return null if such an endpoint does not exists
 */
fun UsbInterface.sendingEndpointOrNull(endpointIndex: Int) = sendingEndpoints().firstOrNull { usbEndpoint ->
    usbEndpoint.endpointNumber == endpointIndex
}

/**
 * Retrieve all receiving endpoints in an interface.  The list may be empty.
 */
fun UsbInterface.receivingEndpoints() = allEndpoints().filter {  usbEndpoint ->
    usbEndpoint.direction == UsbConstants.USB_DIR_IN
}

/**
 * Retrieve all sending endpoints in an interface.  The list may be empty.
 */
fun UsbInterface.sendingEndpoints() = allEndpoints().filter {  usbEndpoint ->
    usbEndpoint.direction == UsbConstants.USB_DIR_OUT
}

/**
 * Retrieve all  endpoints in an interface.  The list may be empty.
 *
 * @return immutable list of [UsbEndpoint]
 */
fun UsbInterface.allEndpoints() = mutableListOf<UsbEndpoint>().apply {
    for (endpointIndex in 0 until endpointCount) {
        add(getEndpoint(endpointIndex))
    }
}.toList()
