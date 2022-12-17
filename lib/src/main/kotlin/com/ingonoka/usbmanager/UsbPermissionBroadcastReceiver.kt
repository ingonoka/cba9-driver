/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

/**
 * Receive the results of a USB permission request
 */
class UsbPermissionBroadcastReceiver(
    private val sendChannel: SendChannel<Boolean>,
    private val usbDevice: UsbDevice

) : BroadcastReceiver() {

    /**
     * Receive the result of the user granting or denying access
     */
    override fun onReceive(context: Context?, intent: Intent?) {

        runBlocking {
            val device = intent?.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

            if (ACTION_USB_PERMISSION == intent?.action && device == usbDevice)
                sendChannel.send(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
        }
    }
}