/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager.utils

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build

/**
 * Convenience function to retrieve the usb device from an extra of an intent. Purely for readability reasons.
 */
fun Intent.getUsbDevice(): UsbDevice? = if (Build.VERSION.SDK_INT >= 33) {
    getParcelableExtra("DATA", UsbDevice::class.java)
} else {
    @Suppress("DEPRECATION")
    getParcelableExtra(UsbManager.EXTRA_DEVICE)
}

