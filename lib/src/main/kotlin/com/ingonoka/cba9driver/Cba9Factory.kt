/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver

import com.ingonoka.cba9driver.statelog.ICba9StateLog
import com.ingonoka.cba9driver.statelog.inMemoryCba9StateLog
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.usbmanager.IUsbTransceiver
import com.ingonoka.usbmanager.UsbDriver
import com.ingonoka.usbmanager.UsbDriverFactory
import com.ingonoka.usbmanager.UsbDriverProperties

/**
 * Factory creates new driver instances for CBA9 devices
 */
class Cba9Factory() : UsbDriverFactory, Stringifiable {

    override fun create(adapter: IUsbTransceiver): Result<UsbDriver> = try {

        Result.success(Cba9(cba9Props, stateLog, adapter))

    } catch (e: Exception) {

        Result.failure(e)
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "USB Adapter for CBA9"
        } else {
            "USB Adapter for CBA9/Banknote Acceptor/Innovative Technology Ltd/Vendor ID 6428/Product ID 16644"
        }

    override fun supports(vendorId: Int, productId: Int): Boolean =
        cba9Props.usbProps.vendorId == vendorId && cba9Props.usbProps.productId == productId


    override fun usbProperties(): UsbDriverProperties = cba9Props.usbProps

    companion object {

        var stateLog: ICba9StateLog = inMemoryCba9StateLog()
        var cba9Props: Cba9Properties = Cba9Properties()
    }
}