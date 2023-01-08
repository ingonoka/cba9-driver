/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import org.slf4j.LoggerFactory
import java.util.*

interface UsbDriverFactory {

    fun create(adapter: IUsbTransceiver): Result<UsbDriver>

    fun supports(vendorId: Int, productId: Int): Boolean

    fun usbProperties(): UsbDriverProperties

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

        private val factories = ServiceLoader.load(UsbDriverFactory::class.java)

        fun get(vendorId: Int, productId: Int): Result<UsbDriverFactory> {
            for (factory in factories) {
                if (factory.supports(vendorId, productId)) {
                    return Result.success(factory)
                }
            }
            return Result.failure(Exception("No driver factory for vendor id: $vendorId and product id: $productId"))
        }

        fun supports(vendorId: Int, productId: Int): Boolean = get(vendorId, productId).isSuccess
    }
}