/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import com.ingonoka.usbmanager.enums.UsbTransferMode
import kotlin.time.Duration

data class UsbDriverProperties(
    /**
     * list of names used to identify a driver and for logging purposes
     */
    val names: List<String>,
    /**
     * The driver will reject devices with a different vendor ID.
     */
    val vendorId: Int,
    /**
     * The driver will reject devices with a different product ID.
     */
    val productId: Int,
    /**
     * The driver will send data to this endpoint.
     */
    val sendingEndpointIndex: Int,
    /**
     * The driver will receive data from this endpoint.
     */
    val receivingEndpointIndex: Int,
    /**
     * The driver will use this interface to send/receive data.
     */
    val interfaceIndex: Int,
    /**
     * The driver will use this interface to send/receive control commands.
     */
    val controlInterfaceIndex: Int,
    /**
     * If the [transferMode] has a timeout, then the driver will wait for this time
     * for a response to a "send" request.
     */
    val timeoutSend: Duration,
    /**
     * If the [transferMode] has a timeout, then the driver will wait for this time
     * for a response to a "receive" request.
     */
    val timeoutReceive: Duration,
    /**
     * Timeout for a USB control transfer.
     */
    val timeoutControlTransfer: Duration,
    /**
     * Transfer mode
     *
     * @see UsbTransferMode
     */
    val transferMode: UsbTransferMode
)