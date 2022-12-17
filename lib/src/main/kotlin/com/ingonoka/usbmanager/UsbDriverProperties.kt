package com.ingonoka.usbmanager

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