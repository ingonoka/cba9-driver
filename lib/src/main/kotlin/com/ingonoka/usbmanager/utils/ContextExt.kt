/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager.utils

import android.content.Context
import android.hardware.usb.UsbManager

/**
 * Convenience function to retrieve the Android Usb manager from a context.
 */
fun Context.getUsbManager() = getSystemService(Context.USB_SERVICE) as UsbManager?
