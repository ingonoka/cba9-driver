/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import android.hardware.usb.UsbEndpoint

/**
 * Create a printable string describing the USB endpoint for logging and debugging purposes
 */
fun UsbEndpoint.stringify(indent: String = "", oneLine: Boolean = false): String {

    val sb = StringBuffer()

    if (oneLine) {
        sb.append(
            "Num: $endpointNumber, Adr: " +
                    "$address, Num: $endpointNumber, " +
                    "Dir: ${UsbDirection(direction).stringify(true, "")}, " +
                    "Type: ${type.usbEndpointTypeStringify()}"
        )
    } else {
        sb.appendLine(
            """
                |${indent}Number:               $endpointNumber
                |${indent}Address:              $address
                |${indent}Direction:            ${UsbDirection(direction).stringify(true, "")}
                |${indent}Number:               $endpointNumber
                |${indent}Type:                 ${type.usbEndpointTypeStringify()}
                |${indent}Interval:             $interval
                |${indent}Max Packet Size:      $maxPacketSize
            """.trimMargin()
        )
    }
    return sb.toString()
}
