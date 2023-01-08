/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import com.ingonoka.cba9driver.util.Stringifiable

/**
 * Create a printable string describing the USB direction of an endpoint
 */
@JvmInline
value class UsbDirection(val value: Int) : Stringifiable {
    override fun stringify(short: Boolean, indent: String): String =
        indent + when (value) {
            128 -> "In"
            0 -> "Out"
            else -> "Unknown"
        }
}
