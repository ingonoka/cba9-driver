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
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Manager of Usb devices.
 */
//@Suppress("unused")
class UsbDeviceManager : Stringifiable, CoroutineScope {

    private val logger = LoggerFactory.getLogger(this::class.java.canonicalName!!)

    private val _connectedDrivers = MutableStateFlow<List<UsbDriver>>(listOf())
    val connectedDrivers: StateFlow<List<UsbDriver>> = _connectedDrivers.asStateFlow()

    private val _driverAttachmentEvents = MutableSharedFlow<DriverAttachmentEvent>(128)
    val driverAttachmentEvents: SharedFlow<DriverAttachmentEvent> = _driverAttachmentEvents.asSharedFlow()

    /**
     * Job that processes USB device attachment/detachment events received from the Android system
     */
    private var androidUsbEventsProcessor: Job? = null

    fun <T : UsbDriver> getDriver(klass: KClass<T>): Result<T> =
        connectedDrivers.value.firstOrNull { klass.isInstance(it) }
            ?.let { Result.success(klass.cast(it)) }
            ?: Result.failure(Exception())

    inline fun <reified T : UsbDriver> getAttachmentEvents(): Flow<DriverAttachmentEvent> = driverAttachmentEvents.filter {
        it.driver is T
    }

    /**
     * Start the android USB events processor.
     *
     * @see processAndroidUsbEvents
     */
    fun start(context: Context) {

        androidUsbEventsProcessor = launch(Dispatchers.Default) { processAndroidUsbEvents(context) }
    }

    /**
     * Call [detachDevice] for all devices that are managed by this UsbDeviceManager.
     */
    suspend fun stop() {

        androidUsbEventsProcessor?.cancelAndJoin()

        connectedDrivers.value.forEach { it.close() }

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

                val action = intent?.action
                val actionStr = action?.takeLastWhile { it != '.' }
                val device = intent?.getUsbDevice()

                safeLet(action, device) { a: String, d: UsbDevice ->

                    logger.info("New USB device detected: ${d.productName}")

                    channel.trySendBlocking(AndroidUsbDeviceEvent(a, d))
                        .onSuccess { logger.trace("Submitted new Android Usb event for processing:  ${d.productName} / $actionStr") }
                        .onFailure { logger.error("Failed to submit new Android USB event for processing: ${d.productName} / $actionStr") }


                } ?: throw IllegalStateException()

            }
        }

        /**
         * Attach devices that are already connected
         */
        context.getUsbManager()?.let { manager ->
            manager.deviceList?.forEach {
                val (_, device) = it
                logger.info("Device discovered on startup: ${device.productName} ")

                channel.trySend(AndroidUsbDeviceEvent(UsbManager.ACTION_USB_DEVICE_ATTACHED, device))
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

            if (UsbDriverFactory.supports(device.vendorId, device.productId)) {
                try {
                    when (action) {

                        UsbManager.ACTION_USB_DEVICE_ATTACHED -> attachDevice(context, device)
                            .onSuccess { logger.info("Successfully attached ${device.productName}") }
                            .getOrThrow()

                        UsbManager.ACTION_USB_DEVICE_DETACHED -> detachDevice(androidUsbDeviceEvent.device)
                            .onSuccess { logger.info("Successfully detached ${device.productName}") }
                            .getOrThrow()

                        else -> throw IllegalStateException("Unexpected action: $androidUsbDeviceEvent.action")
                    }
                } catch (e: Exception) {
                    logger.warn("Failed $actionStr for ${androidUsbDeviceEvent.device.productName}: ${e.combineAllMessages(", ")}")
                }

            } else {

                logger.warn("Usb device not supported:${device.productName}")
            }

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

        connectedDrivers.value.filter { it.supports(usbDevice) }.forEach { driver ->
            _driverAttachmentEvents.emit(DriverAttachmentEvent.Detached(driver))
            _connectedDrivers.update { listOf() }
            driver.close()
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

        _connectedDrivers.update { listOfDrivers ->
            listOfDrivers.filter { driver ->
                if (driver.supports(usbDevice)) {
                    logger.warn("New USB device connection event when a driver is already installed.")
                    driver.close()
                    false
                } else {
                    true
                }
            }
        }

        createDriverForDevice(context, usbDevice).getOrThrow()

        Result.success(Unit)

    } catch (e: Exception) {

        Result.failure(Exception("Failed attaching driver to device: ${usbDevice.stringify(oneLine = true)}", e))
    }

    /**
     * Use factory to create the respective device driver.
     *
     * Publish the attachment event (or a failure) via [driverAttachmentEvents]
     *
     */
    private suspend fun createDriverForDevice(context: Context, usbDevice: UsbDevice): Result<Any> = try {

        logger.trace("Creating driver for ${usbDevice.productName}")

        val driver = UsbDriverFactory.get(usbDevice.vendorId, usbDevice.productId)
            .mapCatching { factory ->
                UsbDeviceAdapter(factory.usbProperties(), usbDevice)
                    .start(context)
                    .mapCatching { adapter -> factory.create(adapter).getOrThrow() }
                    .onSuccess { driver ->
                        _driverAttachmentEvents.emit(DriverAttachmentEvent.Attaching(driver))
                        driver.start()
                    }
                    .onSuccess { driver -> _driverAttachmentEvents.emit(DriverAttachmentEvent.Attached(driver)) }
                    .onSuccess { driver -> _connectedDrivers.update { drivers -> drivers + driver } }
                    .onFailure { cause -> _driverAttachmentEvents.emit(DriverAttachmentEvent.Failed(cause)) }
                    .getOrElse { cause -> throw Exception("Usb Driver creation failed", cause) }

            }
            .onFailure { _driverAttachmentEvents.emit(DriverAttachmentEvent.Failed(Exception("Device not supported: $usbDevice"))) }
            .getOrThrow()

        Result.success(driver)

    } catch (e: Exception) {

        Result.failure(e)
    }

    /**
     * Text representation of the USB Device manager's stage
     */
    override fun stringify(short: Boolean, indent: String) = if (short) {
        "${indent}${connectedDrivers.value.joinToString { it.preferredName }}"
    } else {
        "${indent}Attached drivers: ${connectedDrivers.value.joinToString { it.preferredName }}"
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

}

data class AndroidUsbDeviceEvent(val action: String, val device: UsbDevice)