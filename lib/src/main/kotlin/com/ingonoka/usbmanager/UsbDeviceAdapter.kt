/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

//@file:Suppress("unused")

package com.ingonoka.usbmanager

import android.content.Context
import android.hardware.usb.*
import com.ingonoka.cba9driver.hexutils.toHexShortShort
import com.ingonoka.cba9driver.util.combineAllMessages
import com.ingonoka.cba9driver.util.toByteArray
import com.ingonoka.cba9driver.util.toListOfInt
import com.ingonoka.usbmanager.UsbDeviceAdapterLivecycleStates.*
import com.ingonoka.usbmanager.utils.getUsbManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

/**
 * Indicates method used for transfer data for sending and receiving
 *
 */
enum class UsbTransferMode {
    /**
     * Use [UsbDeviceConnection.bulkTransfer]
     */
    BULK,

    /**
     * Use [UsbDeviceConnection.bulkTransfer] with timeout
     */
    BULK_TIMEOUT,

    /**
     * use [UsbRequest.queue] and [UsbDeviceConnection.requestWait]
     */
    REQUEST,

    /**
     * use [UsbRequest.queue] and [UsbDeviceConnection.requestWait] with timeout
     */
    REQUEST_TIMEOUT
}

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

interface IUsbDeviceAdapter : IUsbTransceiver {

    /**
     * The current state of the adapter e.g. attached, detached etc.
     */
    val state: StateFlow<UsbDeviceAdapterLivecycleStates>

    fun compareAndSetState(oldState: UsbDeviceAdapterLivecycleStates, newState: UsbDeviceAdapterLivecycleStates): Result<Boolean>

    /**
     * String that identifies the adapter generically.
     */
    val preferredName: String

    fun hasName(name: String): Boolean

    fun linkAdapterToUsbDevice(context: Context, usbDevice: UsbDevice): Result<Unit>

    fun close()

    companion object {

//        val nullAdapter = object : IUsbDeviceAdapter {
//            override val state: StateFlow<UsbDeviceAdapterLivecycleStates> = MutableStateFlow(NOT_INSTALLED)
//            override fun compareAndSetState(
//                oldState: UsbDeviceAdapterLivecycleStates,
//                newState: UsbDeviceAdapterLivecycleStates
//            ): Result<Boolean> =
//                throw IllegalStateException()
//
//            override val preferredName: String = "NULL"
//            override fun hasName(name: String): Boolean = name.equals("NULL", true)
//            override fun linkAdapterToUsbDevice(context: Context, usbDevice: UsbDevice): Result<Unit> = throw IllegalStateException()
//
//            override fun close() = Unit
//            override fun transceive(data: List<Int>): Result<List<Int>> = throw IllegalStateException()
//            override fun controlCommand(requestType: Int, request: Int, value: Int, data: ByteArray?): Result<Int> =
//                throw IllegalStateException()
//        }
    }
}

/**
 * Adapter for USB devices that either receive or send data to and from a USB
 * device. "props" contains properties for the adapter configuration.
 *
 */
class UsbDeviceAdapter(
    val props: UsbDriverProperties,
) : IUsbDeviceAdapter, CoroutineScope {

    private val logger = LoggerFactory.getLogger(UsbDeviceAdapter::class.java.name)

    private val _state = MutableStateFlow(NOT_INSTALLED)

    /**
     * The current state of the adapter e.g. attached, detached etc.
     */
    override val state = _state.asStateFlow()

    /**
     * Set [state] to [newState]. return a failure if the current [state] is not [oldState]
     */
    override fun compareAndSetState(
        oldState: UsbDeviceAdapterLivecycleStates, newState: UsbDeviceAdapterLivecycleStates
    ): Result<Boolean> = Result.success(_state.compareAndSet(oldState, newState))

    /**
     * @property [usbDevice] ]Device "driven" by this adapter.  The adapter handles one single device
     * at a time.
     */
    private var usbDevice: UsbDevice? = null

    /**
     * @property [usbConnection] Connection to the USB device
     */
    private var usbConnection: UsbDeviceConnection? = null

    /**
     * String that identifies the adapter generically. (If more than one adapter has been instantiated, all of them
     * will have the same adapter name.)
     */
    override val preferredName: String
        get() = "${props.names[0]}(SN:${usbDevice?.serialNumber ?: "N/A"})"

    /**
     * The list of claimed interfaces
     */
    private var listOfClaimedInterfaces: List<UsbInterface> = listOf()

    /**
     * The endpoint for sending data to the device.  Defined by [UsbDriverProperties]
     */
    private var sendingEndpoint: UsbEndpoint? = null

    /**
     * The endpoint for receiving data from the device.  Defined by [UsbDriverProperties]
     */
    private var receivingEndpoint: UsbEndpoint? = null

    fun hasReceivingEndpoint(): Boolean = receivingEndpoint != null

    /**
     * Function only to be called from the UsbManager.  Will attach the adapter to a USB device that has been
     * detected by the OS.
     *
     * The function will claim all available interfaces on the device.  If none of the interfaces can be claimed,
     * this function will have no effect.
     *
     */
    override fun linkAdapterToUsbDevice(context: Context, usbDevice: UsbDevice): Result<Unit> = try {

        _state.update { INSTALLING }

        logger.debug("Attaching ${usbDevice.productName} to adapter")

        val usbManager = context.getUsbManager()
            ?: throw Exception("Could not retrieve Android USB Manager from context.")

        if (!usbManager.hasPermission(usbDevice)) {
            logger.warn("Android USB Manager does not have permission for device: ${usbDevice.deviceName}")
        }

        usbConnection = usbManager.openDevice(usbDevice)

        usbConnection?.let { listOfClaimedInterfaces = claimAllInterfaces(usbDevice, it) }

        usbConnection ?: throw Exception("Could not open Android USB device.")

        if (listOfClaimedInterfaces.isEmpty()) {
            throw Exception(
                "$preferredName: No interfaces on device to listen on ${usbDevice.stringify(oneLine = true)}"
            )
        }

        sendingEndpoint = getSendingEndpoint(props.interfaceIndex, props.sendingEndpointIndex)

        receivingEndpoint = getReceivingEndpoint(props.interfaceIndex, props.receivingEndpointIndex)

        this@UsbDeviceAdapter.usbDevice = usbDevice

        _state.update { INSTALLED }

        Result.success(Unit)

    } catch (e: Exception) {

        _state.update { FAILED }
//        close()
        Result.failure(Exception("Failed to attach adapter to device: ${usbDevice.stringify(oneLine = true)}", e))
    }

    /**
     * The function removes the link to a USB device when the device get detached.  The device itself is not affected.
     *
     * @throws IllegalStateException if there is no adapter connected (which means something went wrong with the attach-broadcast of the
     * USB subsystem)
     *
     */
    override fun close() {

        logger.info("Adapter $preferredName: Detaching device from adapter")

        usbDevice?.listOfAllInterfaces()?.forEach { usbConnection?.releaseInterface(it) }

        usbConnection?.close()

        usbConnection = null

        usbDevice = null

        _state.update { NOT_INSTALLED }
    }

    /**
     * Return true when this adapter supports a device with vendorId and productId defined in [UsbDriverProperties]
     */
    fun supports(usbDevice: UsbDevice): Boolean {

        if (props.vendorId != usbDevice.vendorId || props.productId != usbDevice.productId) return false

        return true

    }

    private fun getSendingEndpoint(interfaceIndex: Int, endpointIndex: Int): UsbEndpoint {
        return listOfClaimedInterfaces.find { usbInterface -> usbInterface.id == interfaceIndex }
            ?.sendingEndpointOrNull(endpointIndex)
            ?: throw IllegalStateException("Adapter $preferredName:  There is no sending Interface with index \"$interfaceIndex\" and endpoint \"$endpointIndex\"")
    }

    private fun getReceivingEndpoint(interfaceIndex: Int, endpointIndex: Int): UsbEndpoint {
        return listOfClaimedInterfaces.find { usbInterface -> usbInterface.id == interfaceIndex }
            ?.receivingEndpointOrNull(endpointIndex)
            ?: throw IllegalStateException("Adapter $preferredName:  There is no receiving Interface with index \"$interfaceIndex\" and endpoint \"$endpointIndex\"")
    }

    /**
     * Claims exclusive access for all interfaces of the USB device connection.
     * Required before sending or receiving data on an interface.
     */
    private fun claimAllInterfaces(usbDevice: UsbDevice, usbDeviceConnection: UsbDeviceConnection):
            List<UsbInterface> =
        mutableListOf<UsbInterface>().apply {
            for (usbInterface in usbDevice.listOfAllInterfaces()) {
                if (usbDeviceConnection.claimInterface(usbInterface, true)) {
                    logger.debug(
                        "$preferredName: Claimed interface: ${
                            usbInterface.stringify(oneLine = true)
                        }    on device ${
                            usbDevice.stringify(oneLine = true)
                        }"
                    )
                    add(usbInterface)
                } else {
                    logger.error("$preferredName: Could not claim interface ${usbInterface.stringify()} on device ${usbDevice.stringify()}")
                }
            }
        }.toList()

    /**
     * Send data to device and wait for response. [props] determines whether
     * synchronized bulk transfer or async queue/await methods are used.
     */
    override fun transceive(data: List<Int>): Result<List<Int>> = when (props.transferMode) {
        UsbTransferMode.BULK_TIMEOUT -> transceiveWithBulkTransfer(data)
        UsbTransferMode.REQUEST_TIMEOUT -> transceiveQueued(data)
        else -> throw RuntimeException("Illegal value for transfer mode")
    }

    /**
     * Send data to endpoint using synchronous bulk transfer
     */
    private fun sendWithBulkTransfer(data: List<Int>, usbEndpoint: UsbEndpoint): Result<Int> =
        try {

            logger.trace("Send /w bulk (${props.timeoutSend}): ${data.toHexShortShort()}")
            val sentThisTime = usbConnection?.bulkTransfer(
                usbEndpoint,
                data.toByteArray(),
                0,
                data.size,
                props.timeoutSend.inWholeMilliseconds.toInt()
            ) ?: throw Exception("Usb Connection not available.")

            if (sentThisTime < 0) {
                throw Exception("$preferredName: Bulk transfer has failed.")
            }

            logger.trace("Adapter $preferredName: Successfully sent $sentThisTime bytes to device: ${data.toHexShortShort()}")

            Result.success(sentThisTime)


        } catch (e: Exception) {
            logger.error("Failed USB send: ${e.combineAllMessages(",")}")
            Result.failure(Exception("Failed USB send", e))
        }

    /**
     * Receive data from endpoint using synchronous bulk transfer
     */
    private fun receiveWithBulkTransfer(usbEndpoint: UsbEndpoint): Result<List<Int>> = try {

        val buf = ByteArray(256)

        val receivedBytes = usbConnection?.let { connection ->

            val numReceived = connection.bulkTransfer(
                usbEndpoint,
                buf,
                0,
                buf.size,
                props.timeoutReceive.inWholeMilliseconds.toInt()
            )

            if (numReceived < 0) {
                throw Exception("$preferredName: Bulk receive has failed.")
            }

            buf.take(numReceived).map { it.toInt() }

        } ?: throw Exception("USB connection not available.")

        Result.success(receivedBytes)

    } catch (e: Exception) {

        Result.failure(Exception("Failed Receive data.", e))
    }

    /**
     * Send data and receive response using bulk transfer
     */
    private fun transceiveWithBulkTransfer(data: List<Int>): Result<List<Int>> = try {

        logger.trace("Transceive with bulk: ${usbDevice?.productName}")

        if (data.isNotEmpty()) {
            val numBytesSent = sendingEndpoint?.let { endPoint ->
                sendWithBulkTransfer(data, endPoint).getOrThrow()
            } ?: throw Exception("Adapter has no sending endpoint for device.")

            logger.trace("Bytes sent to Usb device ${usbDevice?.productName}: $numBytesSent")
        }

        val buf = receivingEndpoint?.let { endPoint ->
            receiveWithBulkTransfer(endPoint).getOrThrow()
        } ?: throw Exception("Adapter has no receiving endpoint for device.")

        logger.trace("Bytes received from Usb device ${usbDevice?.productName}: ${buf.size}, ${buf.toHexShortShort()}")

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(Exception("Failed transceive of data via sync bulk transfer, ${usbDevice?.productName}", e))
    }

    /**
     * Send data and receive response using queue/await functions
     */
    private fun transceiveQueued(data: List<Int>): Result<List<Int>> = try {

        usbConnection?.let { connection ->

            if (data.isNotEmpty()) {
                val sendRequest = UsbRequest()

                sendingEndpoint?.let { sendRequest.initialize(connection, it) }
                    ?: throw Exception("Sending endpoint is null")

                val sendBuffer = ByteBuffer.wrap(data.toByteArray())
                sendRequest.queue(sendBuffer)

                var sendResponse: UsbRequest? = null
                while (sendResponse != sendRequest) {
                    sendResponse = connection.requestWait(props.timeoutSend.inWholeMilliseconds)
                }
            }

            val receiveBuf = ByteBuffer.allocate(256)
            val receiveRequest = UsbRequest()

            receivingEndpoint?.let { receiveRequest.initialize(connection, it) }
                ?: throw Exception("Receiving endpoint is null")

            receiveRequest.queue(receiveBuf)

            var receiveResponse: UsbRequest? = null
            while (receiveResponse != receiveRequest) {
                receiveResponse = connection.requestWait(props.timeoutReceive.inWholeMilliseconds)

            }

            val returnedBytes = ByteArray(receiveBuf.position())
            receiveBuf.get(returnedBytes, 0, receiveBuf.position())

            Result.success(returnedBytes.toListOfInt())

        } ?: throw Exception("No Usb Connection")


    } catch (e: Exception) {

        Result.failure(Exception("Failed transmission of data via async queue/requestWait", e))
    }

    override fun controlCommand(requestType: Int, request: Int, value: Int, data: ByteArray?): Result<Int> = try {
        val dataLength = data?.size ?: 0
        val response: Int = usbConnection?.controlTransfer(
            requestType,
            request,
            value,
            props.controlInterfaceIndex,
            data,
            dataLength,
            props.timeoutControlTransfer.inWholeMilliseconds.toInt()
        ) ?: throw Exception("Failed control transfer")

        Result.success(response)

    } catch (e: Exception) {

        Result.failure(e)
    }

    override fun hasName(name: String): Boolean = props.names.contains(name)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UsbDeviceAdapter) return false

        if (props != other.props) return false
        return true
    }

    override fun hashCode(): Int = props.hashCode()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default


}