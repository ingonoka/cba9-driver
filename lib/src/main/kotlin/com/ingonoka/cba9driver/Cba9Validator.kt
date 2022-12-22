/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver

import com.ingonoka.cba9driver.BanknoteInstruction.*
import com.ingonoka.cba9driver.Cba9ValidatorState.*
import com.ingonoka.cba9driver.data.CountryCode
import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.data.SerialNumber
import com.ingonoka.cba9driver.event.*
import com.ingonoka.cba9driver.response.GetDatasetVersionResponseData
import com.ingonoka.cba9driver.response.SetupResponseData
import com.ingonoka.cba9driver.statelog.ICba9StateLog
import com.ingonoka.cba9driver.util.Stringifiable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

interface ICba9Validator : Stringifiable {
    val serialNumber: SerialNumber
    val configData: SetupResponseData
    val datasetVersion: GetDatasetVersionResponseData

    /**
     * A property holding information about the state of the cashbox of the banknote validator (full, empty, ...)
     */
    val cashbox: Cba9Cashbox

    /**
     * Returns true if and only if validator is in DISABLED state
     */
    fun isEnabled(): Boolean

    /**
     * Publish a list of events. Note that the list will be published as is.  The events are not published individually.
     */
    fun publishEvents(events: List<SspEvent>)

    /**
     * Publish [event] in a list with a single item
     */
    fun publishEvent(event: SspEvent)

    /**
     * Start a job to collect and process new events published via [eventsFlow].
     */
    fun start()

    suspend fun stop()

    /**
     * Observe this flow to get validator events.  Note that events are published as lists of events that
     * have been received from the BNA in response to a single Poll command
     */
    val eventsFlow: SharedFlow<List<SspEvent>>

    /**
     * Collect this read-only flow to react to each change in the state of the validator
     */
    val stateFlow: SharedFlow<Cba9ValidatorStateHolder>

    /**
     * Collect to this flow to get the most recent state of the validator.  State changes
     * may get lost of the collector is not fast enough. use [stateFlow] if every single state change
     * is required.
     */
    val state: StateFlow<Cba9ValidatorStateHolder>

    /**
     * Instruct the driver to move a scanned banknote from escrow into the cashbox. If no banknote is in escrow, then
     * the instruction will be carried out immediately after a banknote was successfully scanned.
     */
    fun acceptBanknote()

    /**
     * Instruct the driver to move a scanned banknote from escrow back to the customer (front bezel).
     * If no banknote is in escrow, then the instruction will be carried out immediately after a banknote was
     * successfully scanned.
     */
    fun rejectBanknote()
}

/**
 * Represents a CBA9 validator.  A validator object maintains the [state] of the device which can be changed by sending new events
 * received from the physical device or generated by driver as a result of successful or failed commands sent to
 * the device.
 *
 * @param initialState The initial state of the device. Default is UNDEFINED
 * @param cashboxCapacity The capacity of the cashbox installed in the validator.  Default is 300
 * @property serialNumber The serial number of the device.  Bey default this is 0, which allows creation of validator
 * objects without having access to a physical device.
 * @property configData The configuration of the device as received in response to a [com.ingonoka.cba9driver.command.SetupRequest] command.
 * @property datasetVersion The dataset version as received by [com.ingonoka.cba9driver.command.GetDatasetVersion]
 */
class Cba9Validator(
    initialState: Cba9ValidatorStateHolder = Cba9ValidatorStateHolder(Instant.DISTANT_PAST, UNDEFINED),
    cashboxCapacity: Int = 300,
    maxLodBetweenCollections: Int = 300_000,
    stateLog: ICba9StateLog,
    override val serialNumber: SerialNumber = SerialNumber(),
    override val configData: SetupResponseData = SetupResponseData(),
    override val datasetVersion: GetDatasetVersionResponseData = GetDatasetVersionResponseData()
) : Stringifiable, ICba9Validator, CoroutineScope {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    /**
     * A property holding information about the state of the cashbox of the banknote validator (full, empty, ...)
     */
    override val cashbox = Cba9Cashbox(stateLog, cashboxCapacity, maxLodBetweenCollections, serialNumber)

    /**
     * A shared flow for publishing the state of the device
     */
    private val _stateFlow = MutableSharedFlow<Cba9ValidatorStateHolder>(128)

    /**
     * Collect this read-only flow to react to change in the state of the validator
     */
    override val stateFlow = _stateFlow.asSharedFlow()

    /**
     * A shared flow for publishing the state of the device
     */
    private val _state = MutableStateFlow(initialState)

    /**
     * Collect this read-only flow to react to change in the state of the validator
     */
    override val state = _state.asStateFlow()

    /**
     * True if validator is in DISABLED state
     */
    override fun isEnabled() = state.value.state != DISABLED

    private val _eventsFlow = MutableSharedFlow<List<SspEvent>>(128, 128, BufferOverflow.DROP_OLDEST)

    /**
     * Observe this flow to get validator events
     */
    override val eventsFlow = _eventsFlow.asSharedFlow()

    /**
     * Determines what is done with a banknote that is already in Escrow or moved into Escrow.
     */
    internal var banknoteInstruction = MutableStateFlow(NONE)

    private val creditEvents = listOf(NoteClearedIntoCashbox::class, NoteCredit::class)

    private val stateTable = mapOf(

        UNDEFINED to mapOf(
            Alive::class to UNDEFINED,
            Synced::class to INITIALIZING,
            SlaveReset::class to INITIALIZING,
            Disabled::class to DISABLED,
            Scanning::class to SCANNING,
            Stacking::class to STACKING,
            Rejecting::class to REJECTING,
        ),

        INITIALIZING to mapOf(
            Alive::class to INITIALIZING,
            ChannelEnabled::class to INITIALIZING,
            Initialising::class to INITIALIZING,
            SlaveReset::class to INITIALIZING,
            ChannelDisable::class to INHIBITED,
            Stacking::class to STACKING,
            Rejecting::class to REJECTING,
            NoteClearedFromFront::class to INITIALIZING,
            NoteClearedIntoCashbox::class to INITIALIZING,
            Disabled::class to DISABLED,
            Enabled::class to READY,
        ),

        READY to mapOf(
            Alive::class to READY,
            Scanning::class to SCANNING,
            Stacking::class to STACKING,
            Stacked::class to READY,
            Rejecting::class to REJECTING,
            Rejected::class to READY,
            StackerFull::class to CASHBOX_FULL,
            Disabled::class to DISABLED,
            AcceptedNote::class to NOTE_IN_ESCROW,
            ChannelEnabled::class to READY,
            Enabled::class to READY,
        ),

        SCANNING to mapOf(
            Alive::class to SCANNING,
            Scanning::class to SCANNING,
            Rejecting::class to REJECTING,
            AcceptedNote::class to NOTE_IN_ESCROW,
            Disabled::class to DISABLED,
        ),

        NOTE_IN_ESCROW to mapOf(
            Alive::class to NOTE_IN_ESCROW,
            Stacking::class to STACKING,
            Rejecting::class to REJECTING,
            Rejected::class to READY,
            Disabled::class to DISABLED,
        ),

        STACKING to mapOf(
            Alive::class to STACKING,
            // On startup the device may be stacking a leftover note when the driver still initializes
            ChannelEnabled::class to STACKING,
            Stacking::class to STACKING,
            NoteCredit::class to STACKING_CREDITED,
            Disabled::class to DISABLED,
            UnsafeJam::class to UNSAFE_JAM,
        ),

        STACKING_CREDITED to mapOf(
            Alive::class to STACKING_CREDITED,
            Stacking::class to STACKING_CREDITED,
            Stacked::class to READY,
            UnsafeJam::class to UNSAFE_JAM,
            Disabled::class to DISABLED,
        ),

        REJECTING to mapOf(
            Alive::class to REJECTING,
            Rejecting::class to REJECTING,
            Rejected::class to READY,
            Disabled::class to DISABLED,
        ),

        UNSAFE_JAM to mapOf(
            Alive::class to UNSAFE_JAM,
            Disabled::class to DISABLED,
        ),

        DISABLED to mapOf(
            Alive::class to READY,
            Disabled::class to DISABLED,
            Enabled::class to READY,
            ChannelEnabled::class to INITIALIZING,
        ),

        CASHBOX_FULL to mapOf(
            Alive::class to CASHBOX_FULL,
            StackerFull::class to CASHBOX_FULL,
            Disabled::class to DISABLED,
        ),

        INHIBITED to mapOf(
            Alive::class to INHIBITED,
            Disabled::class to DISABLED,
            ChannelEnabled::class to INITIALIZING,
        )
    )

    private var previousEvent: SspEvent? = null

    private var eventProcessor: Job? = null

    override fun acceptBanknote() {
        banknoteInstruction.value = ACCEPT
    }

    override fun rejectBanknote() {
        banknoteInstruction.value = REJECT
    }

    override fun publishEvents(events: List<SspEvent>) {
        _eventsFlow.tryEmit(events)
    }

    override fun publishEvent(event: SspEvent) {
        _eventsFlow.tryEmit(listOf(event))
    }

    private suspend fun processEvent(event: SspEvent): Result<Cba9ValidatorState> = try {

        if (event.sspEventCode != previousEvent?.sspEventCode && event.denomination != previousEvent?.denomination) {
            previousEvent = event
            logger.debug("New event to process: ${event.stringify(short = true)}")
        }

        if (event::class in creditEvents) {
            cashbox.addBanknote(event.time, event.denomination)
        }

        if (event is StackerFull) {
            cashbox.setFull()
        }

        val currentState = _state.value

        val newState = stateTable[currentState.state]?.get(event::class)
            ?: throw Exception("Unexpected event ${event.stringify(true)} in state ${currentState.stringify(true)}")

        val denomination = if (newState == READY && currentState.state == READY) {
            Denomination(0, CountryCode.UNKNOWN)
        } else if (newState == STACKING && currentState.state in listOf(NOTE_IN_ESCROW, STACKING)) {
            currentState.denomination
        } else {
            event.denomination
        }

        val newStateHolder = Cba9ValidatorStateHolder(Clock.System.now(), newState, denomination)

        _stateFlow.emit(newStateHolder)

        if (currentState.state != newState && currentState.denomination != denomination) {
            logger.trace("CBA9 state transition: ${currentState.state.name} + ${event.sspEventCode.name} = ${newState.name}")
        }

        if (currentState.state != newState) {
            logger.debug(
                "BNA State change: ${currentState.stringify(true)} (${event.stringify(true)}) -> ${state.value.stringify(true)}"
            )
            _state.emit(newStateHolder)
        }

        Result.success(newState)

    } catch (e: Exception) {

        Result.failure(e)
    }

    override fun start() {
        eventProcessor = launch(Dispatchers.Default) {

            eventsFlow.collect { events ->

                if (events.isNotEmpty()) {

//                    if (events.size == 1) {
//                        processEvent(events[0])
//                    } else {
//                        when (events.last()) {
//                            is AcceptedNote -> processEvent(events.last())
//                            else -> events.forEach { processEvent(it) }
//                        }
//                    }

                    events.forEach { processEvent(it) }

                    when (state.value.state) {
                        NOTE_IN_ESCROW -> if (banknoteInstruction.value !in listOf(ACCEPT, REJECT)) banknoteInstruction.update { HOLD }
                        else -> banknoteInstruction.update { NONE }
                    }
                }
            }
        }
    }

    override suspend fun stop() {
        eventProcessor?.cancelAndJoin()
    }

    override fun toString() = stringify()

    override fun stringify(short: Boolean, indent: String): String =
        indent + "sn:${serialNumber.stringify(short, indent)}, " +
                "ds:${datasetVersion.stringify(short, indent)}, " +
                "cf:[${configData.stringify(short, indent)}]"

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

}

/**
 * The instruction of the driver determines whether a scanned banknote will be held in escrow, moved to the cashbox
 * or returned to the customer.
 */
enum class BanknoteInstruction {
    /**
     * No instruction available.
     */
    NONE,

    /**
     * HOLD is the default instruction and is set by the driver automatically.
     */
    HOLD,

    /**
     * ACCEPT instructs the driver to move the present or future banknote from escrow into the cashbox
     */
    ACCEPT,

    /**
     * Instructs the driver to move the present ot future banknote from escrow back to the front bezel.
     */
    REJECT
}