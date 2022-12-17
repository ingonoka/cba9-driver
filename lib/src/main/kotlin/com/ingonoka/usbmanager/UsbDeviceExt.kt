/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbInterface

/**
 * Create a printable string describing the USB device for logging and debugging purposes
 */
fun UsbDevice.stringify(indent: String = "", oneLine: Boolean = false): String = buildString {
        if (oneLine) {
            append("${indent}$productName, ")
            append("SN: ${serialNumber ?: "N/A"}, ")
            append("VID: $vendorId (${vendorId.toString(16)}), ")
            append("PID: $productId (${productId.toString(16)})")
        } else {
            appendLine(
                """
                |${indent}Name:                 $deviceName
                |${indent}Serial No:            ${serialNumber ?: "N/A"}
                |${indent}Class:                ${deviceClass.usbClassStringify()}
                |${indent}Device Id:            $deviceId
                |${indent}Product Id:           $productId (${productId.toString(16)})
                |${indent}Vendor Id:            $vendorId (${vendorId.toString(16)})
                |${indent}Protocol:             ${deviceProtocol.usbDeviceProtocolStringify()}
                |${indent}Number of interfaces: $interfaceCount
            """.trimMargin()
            )

            for (index in 0 until interfaceCount) {
                append("$indent\tInterface: $index\n")
                append(getInterface(index).stringify(indent + "\t\t"))
            }
        }
    }

    /**
     * Provide a list of all interface available for the USB device
     */
    fun UsbDevice.listOfAllInterfaces(): List<UsbInterface> =

        mutableListOf<UsbInterface>().apply {

            val numberOfInterfaces = this@listOfAllInterfaces.interfaceCount
            for (interfaceIndex in 0 until numberOfInterfaces) {
                add(this@listOfAllInterfaces.getInterface(interfaceIndex))
            }
        }.toList()
