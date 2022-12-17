/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import android.hardware.usb.UsbConstants.*

/**
 * Create a printable string describing the USB endpoint type for logging and debugging purposes
 */
fun Int.usbEndpointTypeStringify(): String {

    return when (this) {
        USB_ENDPOINT_XFER_CONTROL -> "Control Endpoint ($USB_ENDPOINT_XFER_CONTROL)"
        USB_ENDPOINT_XFER_ISOC -> "Isochronous endpoint ($USB_ENDPOINT_XFER_ISOC)"
        USB_ENDPOINT_XFER_BULK -> "Bulk Endpoint ($USB_ENDPOINT_XFER_BULK)"
        USB_ENDPOINT_XFER_INT -> "Interrupt Endpoint ($USB_ENDPOINT_XFER_INT)"
        else -> "Unknown type: $this"
    }
}
