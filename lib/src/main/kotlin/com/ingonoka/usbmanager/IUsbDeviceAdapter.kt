/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import android.content.Context
import com.ingonoka.usbmanager.enums.UsbDeviceAdapterLivecycleStates
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementing classes provide the [state] of a USB adapter.
 */
interface IUsbDeviceAdapter {

    /**
     * The current state of the adapter e.g. attached, detached etc.
     */
    val state: StateFlow<UsbDeviceAdapterLivecycleStates>

    /**
     * Links the adapter to the USB device
     */
    fun start(context: Context): Result<IUsbDeviceAdapter>

    /**
     * Closes the link to the USB device
     */
    fun close()
}