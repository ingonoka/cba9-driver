/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import kotlinx.coroutines.channels.SendChannel

/**
 * Get all devices matching the vendor id and product id.  If vendor id or
 * product id are zero they match all possible values respectively.
 *
 * @return List of [UsbDevice] objects.  The list may be empty
 */

fun UsbManager.getConnectedDevices(vendorId: Int, productId: Int): List<UsbDevice> =

    deviceList.values.filter {
        (productId == 0 || it.productId == 0 || productId == it.productId) &&
                (vendorId == 0 || it.vendorId == 0 || vendorId == it.vendorId)
    }

/**
 * Human-readable list of all devices for logging and debugging purposes.
 *
 * @param indent List will be indented with this string at the beginning of the line
 * @param oneLine If true, will use one line per device only
 */
@Suppress("unused")
fun UsbManager.stringifyDeviceList(indent: String = "", oneLine: Boolean = false) =
    deviceList.values.fold(StringBuffer()) { acc, usbDevice ->
        acc.append(usbDevice.stringify(indent, oneLine))
    }.toString()

/**
 *
 */
const val ACTION_USB_PERMISSION = "com.ingonoka.usb_driver.USB_PERMISSION"

/**
 * Request permission for USB device.
 *
 * @param context
 * @param sendChannel Callback will send a message via this channel when the user has chosen the permission level.
 * The message will contain `true` is the user granted permission, `false` otherwise
 * @param usbDevice The device to ask for permission
 *
 * @return Return `true` if permission had already been granted.  Return `false` if permission was requested.
 */
@Suppress("unused")
fun UsbManager.getPermissionForDevice(context: Context, sendChannel: SendChannel<Boolean>, usbDevice: UsbDevice) =

    if (hasPermission(usbDevice))

        true
    else {

        context.registerReceiver(
            UsbPermissionBroadcastReceiver(sendChannel, usbDevice),
            IntentFilter(ACTION_USB_PERMISSION)
        )

        requestPermission(usbDevice, PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE))

        false
    }