/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.usbmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.ingonoka.cba9driver.Cba9AdapterFactory
import com.ingonoka.cba9driver.Cba9Properties
import com.ingonoka.cba9driver.ICba9
import com.ingonoka.cba9driver.isCba9
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.combineAllMessages
import com.ingonoka.cba9driver.util.safeLet
import com.ingonoka.usbmanager.utils.getUsbDevice
import com.ingonoka.usbmanager.utils.getUsbManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

/**
 * Enumeration of lifecycle states for a USB adapter.
 */
enum class UsbDeviceAdapterLivecycleStates {
    NOT_INSTALLED, INSTALLING, INSTALLED, FAILED
}

/**
 * Manager of CBA9 and CP210x devices.
 */
//@Suppress("unused")
class UsbDeviceManager : Stringifiable, CoroutineScope {

    private val logger = LoggerFactory.getLogger(this::class.java.canonicalName!!)

    private val _connectedCba9 = MutableStateFlow<ICba9?>(null)
    val connectedCba9 = _connectedCba9.asStateFlow()

    private val _cba9AttachmentEvents = MutableSharedFlow<Cba9AttachmentEvent>(128)
    private val cba9AttachmentEvents = _cba9AttachmentEvents.asSharedFlow()

    /**
     * Job that processes USB device attachment/detachment events received from the Android system
     */
    private var androidUsbEventsProcessor: Job? = null

    /**
     * Start the android USB events processor.
     *
     * @see processAndroidUsbEvents
     */
    fun start(context: Context) {

        androidUsbEventsProcessor = launch(Dispatchers.Default) { processAndroidUsbEvents(context) }
    }

    /**
     * Call [detachDevice] for all devices that are connected.
     */
    suspend fun stop(context: Context) {

        androidUsbEventsProcessor?.cancelAndJoin()

        context.getUsbManager()?.deviceList?.values?.forEach { androidUsbDevice ->
            detachDevice(androidUsbDevice)
        }
    }

    /**
     * Converts the Android broadcast interface into a cold flow.
     *
     * The resulting flow emits an [AndroidUsbDeviceEvent] every time an ACTION_USB_DEVICE_ATTACHED or ACTION_USB_DEVICE_DETACHED
     * action is received.
     */
    private fun doReceive(context: Context) = callbackFlow {

        /**
         * Callback to be called when USB device is attached or detached
         */
        val callback = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {

                safeLet(intent?.action, intent?.getUsbDevice()) { s: String, u: UsbDevice ->
                    channel.trySendBlocking(AndroidUsbDeviceEvent(s, u))
                        .onSuccess { logger.debug("Submitted new Android Usb event for processing:  ${u.productName} / $s") }
                        .onFailure { logger.debug("Failed to submit new Android USB event for processing: ${u.productName} / $s") }
                } ?: throw IllegalStateException()

            }
        }

        /**
         * attach devices that are already connected
         */
        context.getUsbManager()?.let { manager ->
            manager.deviceList?.filter {
                val (_, device) = it
                logger.info("Device discovered on startup: ${device.stringify("", true)} ")
                device.isCba9()
            }?.forEach {
                val (_, device) = it
                channel.trySendBlocking(AndroidUsbDeviceEvent(UsbManager.ACTION_USB_DEVICE_ATTACHED, device))
            }
        }

        /**
         * Register the callback
         */
        try {
            context.registerReceiver(callback, IntentFilter().also {
                it.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                it.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            })
        } catch (e: IllegalArgumentException) {
            logger.error("Failed to register USB Device Manager as broadcast receiver.", e)
        } catch (e: Exception) {
            throw Exception("Failed to register USB Device Manager as broadcast receiver.", e)
        }

        /**
         * Suspend until the callback flow is closed
         */
        awaitClose { context.unregisterReceiver(callback) }

    }.buffer(10)

    /**
     * Collect attachment events from the callback flow and call [attachDevice] or [detachDevice]
     * according to the action of the event.
     */
    private suspend fun processAndroidUsbEvents(context: Context) = try {

        logger.info("Start processing of Android Usb events")

        doReceive(context).onEach { androidUsbDeviceEvent ->

            val (action, device) = androidUsbDeviceEvent

            val actionStr = action.takeLastWhile { it != '.' }
            logger.info("Received $actionStr for ${device.productName}")

            try {
                when (action) {

                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> attachDevice(context, device)
                        .onSuccess { logger.info("Successfully attached ${device.productName}") }
                        .onFailure { logger.error("Failed attachment of ${device.productName}") }
                        .getOrThrow()

                    UsbManager.ACTION_USB_DEVICE_DETACHED -> detachDevice(androidUsbDeviceEvent.device)
                        .onSuccess { logger.info("Successfully detached ${device.productName}") }
                        .onFailure { logger.error("Failed detachment of ${device.productName}") }
                        .getOrThrow()

                    else -> throw IllegalStateException("Unexpected action: $androidUsbDeviceEvent.action")
                }
            } catch (e: Exception) {
                logger.warn("Failed $actionStr for ${androidUsbDeviceEvent.device.productName}: ${e.combineAllMessages(", ")}")
            }

            logger.info("Finished processing $actionStr for ${androidUsbDeviceEvent.device.productName}")
        }.collect()

    } catch (e: CancellationException) {

        logger.info("Cancelled processing of Android Usb events")

    } catch (e: Exception) {

        logger.error("Failure processing Android USB events", e)
    }

    /**
     * Process a device that has been detached from the terminal
     */
    private suspend fun detachDevice(usbDevice: UsbDevice): Result<Unit> = try {

        logger.debug("Detaching driver for ${usbDevice.productName}")

        when {
            usbDevice.isCba9() -> {
                connectedCba9.value?.apply {
                    _cba9AttachmentEvents.emit(Cba9AttachmentEvent.Detached(this))
                    _connectedCba9.update { null }
                    close()
                } ?: logger.warn("Received CBA9 detachment event, but no CB9 attached.")
            }

            else -> logger.warn("Detachment event for unsupported device: $usbDevice")

        }

        logger.debug("Finished detaching driver for ${usbDevice.productName}")

        Result.success(Unit)

    } catch (e: Exception) {

        Result.failure(e)
    }

    /**
     * Attached a device. If a driver for the device exists, it will be installed and linked to the
     * device.
     */
    private suspend fun attachDevice(context: Context, usbDevice: UsbDevice): Result<Unit> = try {

        logger.trace("Attaching driver for ${usbDevice.productName}")

        if (usbDevice.isCba9() && connectedCba9.value != null) {
            logger.warn("New Cba9 connection event when a CBA9 is still connected.")
            connectedCba9.value?.close()
            _connectedCba9.update { null }
        }

        createDriverForDevice(context, usbDevice).getOrThrow()

        Result.success(Unit)

    } catch (e: Exception) {

        Result.failure(Exception("Failed attaching driver to device: ${usbDevice.stringify(oneLine = true)}", e))
    }

    /**
     * Use factory to create the respective device driver.
     *
     * Publish the attachment event (or a failure) via [cba9AttachmentEvents]
     *
     */
    private suspend fun createDriverForDevice(context: Context, usbDevice: UsbDevice): Result<Unit> = try {

        logger.trace("Creating driver for ${usbDevice.productName}")

        when {
            usbDevice.isCba9() -> {
                _cba9AttachmentEvents.emit(Cba9AttachmentEvent.Attaching())

                val usbDeviceAdapter = UsbDeviceAdapter(Cba9Properties.usbProps)
                usbDeviceAdapter.linkAdapterToUsbDevice(context, usbDevice)
                    .mapCatching { Cba9AdapterFactory().createCba9(usbDeviceAdapter).getOrThrow() }
                    .mapCatching { cba9 -> cba9.start().getOrThrow() }
                    .onSuccess { cba9 -> _cba9AttachmentEvents.emit(Cba9AttachmentEvent.Attached(cba9)) }
                    .onSuccess { cba9 -> _connectedCba9.update { cba9 } }
                    .onFailure { cause -> _cba9AttachmentEvents.emit(Cba9AttachmentEvent.Failed(cause)) }
                    .getOrElse { cause -> throw Exception("CBA9 creation failed", cause) }
            }

            else -> throw NotImplementedError("Usb device not supported: ${usbDevice.stringify()}")
        }

        logger.trace("Finished creating driver for ${usbDevice.productName}")

        Result.success(Unit)

    } catch (e: Exception) {

        Result.failure(e)
    }

    /**
     * Text representation of the USB Device manager's stage
     */
    override fun stringify(short: Boolean, indent: String) = if (short) {
        "${indent}Cba9:${connectedCba9.value}"
    } else {
        "${indent}Attached devices: Cba9:${connectedCba9.value}"
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

}

data class AndroidUsbDeviceEvent(val action: String, val device: UsbDevice)