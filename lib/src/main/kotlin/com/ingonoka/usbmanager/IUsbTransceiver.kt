/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

/**
 * Implementing classes provide functions to send/receive data and control commands to/from USB devices
 */
interface IUsbTransceiver {
    /**
     * Send [data] to a USB device and return a response
     *
     * @return [List] of bytes received back
     */
    fun transceive(data: List<Int> = emptyList()): Result<List<Int>>

    /**
     * Send control commands to a USB device
     */
    fun controlCommand(requestType: Int, request: Int, value: Int, data: ByteArray? = null): Result<Int>

}