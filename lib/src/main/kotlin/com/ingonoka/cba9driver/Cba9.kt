/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver

import com.ingonoka.cba9driver.command.*
import com.ingonoka.cba9driver.data.CountryCode.*
import com.ingonoka.cba9driver.event.*
import com.ingonoka.cba9driver.response.GenericResponseCode.*
import com.ingonoka.cba9driver.response.GenericResponseCode.UNKNOWN
import com.ingonoka.cba9driver.response.GetCountersResponseData
import com.ingonoka.cba9driver.response.PollResponseData
import com.ingonoka.cba9driver.response.SspResponse
import com.ingonoka.cba9driver.statelog.ICba9StateLog
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.combineAllMessages
import com.ingonoka.usbmanager.UsbDeviceAdapter
import com.ingonoka.usbmanager.UsbDeviceAdapterLivecycleStates.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

interface ICba9 : Stringifiable {
    /**
     * Collect this flow to be informed of changes in the device managed by this driver.
     */
    val cba9Validator: StateFlow<ICba9Validator?>

    fun getValidator(): Result<ICba9Validator>
    fun getValidatorOrNull(): ICba9Validator?

    /**
     * Send GetCounters command to device and return response
     */
    suspend fun getAuditCounters(): Result<GetCountersResponseData>

    /**
     * Get audit counters from device and save in [ICba9StateLog]
     */
    suspend fun saveAuditCounters(time: Instant): Result<Unit>

    /**
     * Reset all counters kept by the CBA9 to 0.
     *
     * The CBA9 keeps its own counters for the number of banknotes that have been stacked or rejected.  The counters
     * are not automatically reset when the cashbox is emptied.
     */
    @Suppress("unused")
    suspend fun resetCashboxCounters(time: Instant): Result<Unit>

    /**
     * Send enable command to validator and set validator status to active.
     *
     * Does not do anything if validator is already enabled and active
     */
    suspend fun enableValidator(): Result<SspResponse>

    /**
     * Send disable command to validator and set validator status to inactive
     */
    suspend fun disableValidator(): Result<SspResponse>

    suspend fun start(): Result<ICba9>
    suspend fun close()
}

/**
 * Provides access to a CBA9 BNA, using [usbAdapter] to send commands and receive data via USB. The [stateLog] is used
 * to keep track of accepted banknotes, cash collection events, and to store audit counters received from the BNA.
 *
 * Configuration parameters are provided in [props].
 */
class Cba9(
    private val props: Cba9Properties = Cba9Properties(),
    val stateLog: ICba9StateLog,
    private val usbAdapter: UsbDeviceAdapter
) : ICba9, CoroutineScope {

    init {
        require(usbAdapter.hasName("CBA9"))
    }

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    init {
        performReset = performReset ?: props.resetOnAttachment
    }

    /**
     * Holding the Mutex will ensure that function is run only once at a time.
     */
//    private val executionMutex = Mutex(false)


    /**
     * The validator device that is managed by this driver. Check whether the device is valid by verifying
     * that the serial number is not 0.
     */
    private val _cba9Validator = MutableStateFlow<ICba9Validator?>(null)

    /**
     * Collect this flow to be informed of changes in the device managed by this driver.
     */
    override val cba9Validator = _cba9Validator.asStateFlow()

    /**
     * This job keeps the validator "alive" by sending POLL or HOLD commands in configurable intervals.
     */
    private var pollingJob: Job? = null

    /**
     * Job sending increasing Long to the [ticker] flow in intervals determined by [props]
     */
    private var tickerJob: Job? = null

    /**
     * Interval in which the [tickerJob]  sends ticks
     */
    private var tickerInterval = props.pollInterval

    /**
     * State flow that emits increasing Long numbers as received from the [tickerJob]
     */
    private val ticker = MutableStateFlow(0L)

    /**
     * Switch to deactivate the ticker
     */
    private var tickerIsActive = true

    /**
     * The USB driver will call this function after a USB connection has been established. For the CBA9 driver
     * this function will go through the sequence of setting up the BNA and enabling it.  If successful, the
     * device will be in READY state and start accepting banknotes via the front bezel
     */
    override suspend fun start(): Result<ICba9> = try {

        logger.debug("Initialize CBA9")

        delay(1000)

        syncAndWait(10, 5.seconds.inWholeMilliseconds)

        usbAdapter.setHostProtocolVersion(props.protocolVersion).getOrThrow()

        val configData = usbAdapter.getConfig().getOrThrow()

        logger.info(configData.stringify(true))

        val serialNumberResponseData = usbAdapter.getSerialNumber().getOrThrow()

        logger.info(serialNumberResponseData.stringify(false, ""))

        val datasetVersion = usbAdapter.getDatasetVersion().getOrThrow()

        logger.info(datasetVersion.stringify(false))

        val newCba9Validator = Cba9Validator(
            cashboxCapacity = props.cashboxCapacity,
            maxLodBetweenCollections = props.maxLoadBetweenCollections,
            stateLog = stateLog,
            serialNumber = serialNumberResponseData.serialNumber,
            configData = configData,
            datasetVersion = datasetVersion
        )

        newCba9Validator.start()

        _cba9Validator.update {
            logger.trace("Updating cbaValidator property")
            newCba9Validator
        }

        newCba9Validator.publishEvent(Synced())

        pollAndPublish(newCba9Validator)

        usbAdapter.setInhibits(configData.bankNoteDenominations, props.acceptedDenominations).getOrThrow()

        val enabledDenominations = configData.bankNoteDenominations.getSubset(props.acceptedDenominations)

        newCba9Validator.publishEvent(ChannelEnabled(enabledDenominations))

        getAuditCounters().map {
            stateLog.updateDeviceCounters(
                Clock.System.now(),
                serialNumberResponseData.serialNumber.value,
                it
            )
        }

        usbAdapter.enable()
            .onSuccess { newCba9Validator.publishEvent(Enabled()) }
            .getOrThrow()

        tickerJob = startTickerJob()

        pollingJob = startPolling(newCba9Validator)

        Result.success(this)

    } catch (e: Exception) {

        Result.failure(Exception("Failed setup for CBA9 device", e))
    }

    override suspend fun close() {

        logger.debug("Closing CBA9")

        try {
            if (tickerJob?.isActive == true) tickerJob?.cancelAndJoin()
            if (pollingJob?.isActive == true) pollingJob?.cancelAndJoin()
        } catch (e: Exception) {
            logger.warn("Cancellation of \"tickerJob\" or \"pollingJob\" was interrupted")
        }

        usbAdapter.close()

        logger.debug("Deactivating validator")
        cba9Validator.value?.stop() ?: logger.warn("No validator to deactivate")
        _cba9Validator.update { null }
    }

    override fun getValidator(): Result<ICba9Validator> = getValidatorOrNull()?.let {
        Result.success(it)
    } ?: Result.failure(Exception())

    override fun getValidatorOrNull(): ICba9Validator? = cba9Validator.value

    /**
     * Send enable command to validator and set validator status to active.
     *
     * Does not do anything if validator is already enabled and active
     */
    override suspend fun enableValidator(): Result<SspResponse> = cba9Validator.value?.let {
        if (!it.isEnabled()) {
            usbAdapter.enable()
        } else {
            Result.failure(Exception("BNA already in enabled state"))
        }
    } ?: Result.failure(Exception("No Validator"))

    /**
     * Send disable command to validator and set validator status to inactive
     */
    override suspend fun disableValidator(): Result<SspResponse> = cba9Validator.value?.let {
        if (it.isEnabled()) {
            usbAdapter.disable()
        } else {
            Result.failure(Exception("BNA already in disabled state"))
        }
    } ?: Result.failure(Exception("No Validator"))

    /**
     * Send GetCounters command to device and return response
     */
    override suspend fun getAuditCounters(): Result<GetCountersResponseData> = try {

        val result = usbAdapter.getCounters().getOrThrow()

        Result.success(result)

    } catch (e: Exception) {

        Result.failure(Exception("Failed to get audit counters from device", e))
    }

    /**
     * Send sync commands every [interval] ms (default 900 ms) until there is a successful response or
     * maximum tries [maxTries] (default 10) is reached.
     *
     * This is the only command that is always using the same sequence bit (1) regardless of the current
     * value of the sequence bit.
     *
     * @throws TimeoutException if no success after [maxTries]
     */
    private suspend fun syncAndWait(maxTries: Int = 10, interval: Long = 900L) {

        var tries = maxTries
        var sspResponseForSync = UNKNOWN

        while (sspResponseForSync != OK) {
            if (tries-- > 0) {
                SspCommand.sequence = 1
                sspResponseForSync = usbAdapter.sync()
                    .map { it.genericResponseCode }
                    .getOrDefault(FAIL)
                if (sspResponseForSync != OK) delay(interval)
            } else {
                throw TimeoutException("CBA9 Driver did not get a response to sync command after $maxTries tries")
            }
        }
        logger.debug("Sync and wait finished")
    }

    /**
     * Reset all counters kept by the CBA9 to 0.
     *
     * The CBA9 keeps its own counters for the number of banknotes that have been stacked or rejected.  The counters
     * are not automatically reset when the cashbox is emptied.
     */
    @Suppress("unused")
    override suspend fun resetCashboxCounters(time: Instant): Result<Unit> = try {

        cba9Validator.value?.cashbox?.setEmpty(time) ?: throw Exception("No validator")

        Result.success(Unit)

    } catch (e: Exception) {

        Result.failure(e)
    }

    private fun startPolling(validator: Cba9Validator) = launch {

        try {
            combine(
                validator.banknoteInstruction,
                ticker
            ) { instruction, _ ->

                tickerIsActive = false

                sendCommand(validator, instruction)

                tickerIsActive = true

            }.catch {

                logger.error("Failure in polling loop", it)

            }.collect()

        } catch (e: CancellationException) {

            logger.info("Polling loop cancelled")

        } catch (e: Exception) {

            logger.error("Polling loop interrupted", e)
        }
    }

    private suspend fun pollAndPublish(validator: Cba9Validator): PollResponseData =
        usbAdapter.poll(validator)
            .onSuccess {
//                if (it.eventList.intersect(listOf(Cba9ValidatorState.SCANNING, Cba9ValidatorState.NOTE_IN_ESCROW)).isNotEmpty()) {
//                    validator.banknoteInstruction.compareAndSet(BanknoteInstruction.NONE, BanknoteInstruction.HOLD)
//                }
                validator.publishEvents(it.eventList)
            }
            .onFailure {
                logger.warn("Failed poll command: ${it.combineAllMessages(",")}")
            }
            .getOrThrow()

    private suspend fun sendCommand(validator: Cba9Validator, instruction: BanknoteInstruction) {

        tickerInterval = props.pollInterval

        when (instruction) {
            BanknoteInstruction.HOLD -> {
                usbAdapter.holdBanknote().onFailure { logger.warn("Failed HOLD execution", it) }
                tickerInterval = props.holdInterval
            }

            BanknoteInstruction.REJECT -> {
                usbAdapter.ejectBanknote().onFailure { logger.warn("Failed REJECT execution", it) }
                pollAndPublish(validator)
            }

            BanknoteInstruction.ACCEPT, BanknoteInstruction.NONE -> {
                pollAndPublish(validator)
            }
        }
    }

    override fun toString() = stringify()

    override fun stringify(short: Boolean, indent: String): String = if (short) {
        indent + "${_cba9Validator.value}"
    } else {
        """
            |
        """.trimMargin()
    }

    private fun startTickerJob(period: Duration = ZERO, initialDelay: Duration = ZERO) = launch(Dispatchers.Default) {
        delay(initialDelay)
        while (isActive) {
            delay(if (period == ZERO) tickerInterval else period)
            if (tickerIsActive) ticker.emit(ticker.value + 1)
        }
    }

    /**
     * Read the counters from the BNA and save the counters in the Cba9StateLog
     */
    override suspend fun saveAuditCounters(time: Instant): Result<Unit> = try {

        getValidator()
            .mapCatching {
                val sn = it.serialNumber.value
                val counters = getAuditCounters().getOrThrow()
                Pair(sn, counters)
            }
            .mapCatching {
                val (sn, counters) = it
                stateLog.updateDeviceCounters(time, sn, counters)
            }
            .getOrThrow()

    } catch (e: Exception) {

        Result.failure(e)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    companion object {
        /**
         * If this is true, then driver should send a reset command without further initialization.
         *
         * The reset will cause the CBA9 to drop the USB connection, so that the adapter will be discarded.
         *
         */
        var performReset: Boolean? = null
    }
}

