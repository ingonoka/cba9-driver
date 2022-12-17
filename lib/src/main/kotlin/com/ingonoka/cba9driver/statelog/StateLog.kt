package com.ingonoka.cba9driver.statelog

import android.content.Context
import androidx.room.Room
import com.ingonoka.cba9driver.data.CountryCode
import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.response.GetCountersResponseData
import kotlinx.coroutines.flow.asFlow
import kotlinx.datetime.Instant
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds

/**
 * Create an [ICba9StateLog] that is backed by an in-memory list
 */
fun inMemoryCba9StateLog(): ICba9StateLog = Cba9StateLog(InMemoryStateLogStore())

/**
 * Create an [ICba9StateLog] that is backed by a Room database
 */
fun roomCba9StateLog(context: Context): ICba9StateLog = Cba9StateLog(
    RoomStateLogStore(
        Room.databaseBuilder(context, StateLogDb::class.java, "cba9CashboxFillLog").build()
    )
)


/**
 * An implementation of [ICba9StateLog]
 */
private class Cba9StateLog(private val store: StateLogStore) : ICba9StateLog {

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    override fun deleteOlderThan(time: Instant) {
        store.deleteOlderThan(time)
    }

    override fun getAllRecords(serialNumber: Long): List<StateLogEntry> = store.getAll(serialNumber)

    override fun updateCounters(time: Instant, serialNumber: Long, counters: Map<Denomination, Int>): Result<Unit> = try {

        val mostRecentTimestamp = store.getMostRecentCounters(serialNumber)
            .map { logEntries -> logEntries.maxByOrNull { max -> max.time }}
            .getOrDefault(time)

        store.insertAll(stateLogEntry {
            this.event = Cba9StateLogEvent.UPDATE_COUNTERS
            this.serialNumber = serialNumber
            this.time = if(mostRecentTimestamp == time.toEpochMilliseconds()) time + 1.milliseconds else time
            banknoteCounters(counters)
        })

        Result.success(Unit)

    } catch(e: Exception) {

        Result.failure(Exception("Failed to log banknote counters", e))
    }

    override fun emptyCashbox(time: Instant, serialNumber: Long): Result<Unit> = try {
        store.insertAll(stateLogEntry {
            this.event = Cba9StateLogEvent.EMPTY_CASHBOX
            this.serialNumber = serialNumber
            this.time = time
        })
        Result.success(Unit)

    } catch(e: Exception) {

        Result.failure(Exception("Failed to log empty cashbox",e))
    }

    override fun updateDeviceCounters(
        time: Instant,
        serialNumber: Long,
        getCountersResponseData: GetCountersResponseData
    ): Result<Unit> = try {
        val mostRecentTimestamp = store.getMostRecentAudit(serialNumber)?.time
        store.insertAll(stateLogEntry {
            this.event = Cba9StateLogEvent.DEVICE_COUNTERS
            this.serialNumber = serialNumber
            this.time = if(mostRecentTimestamp == time) time + 1.milliseconds else time
            countersResponseData(getCountersResponseData)
        })

        Result.success(Unit)

    } catch(e: Exception) {

        Result.failure(e)
    }

    override fun getCurrentCounters(serialNumber: Long): Result<Map<Denomination, Int>> = try {
        val res = store.getMostRecentCounters(serialNumber)
            .mapCatching { it[0] }
            .map {
                mapOf(
                    Denomination(20, CountryCode.PHP) to it.banknote20,
                    Denomination(50, CountryCode.PHP) to it.banknote50,
                    Denomination(100, CountryCode.PHP) to it.banknote100,
                    Denomination(200, CountryCode.PHP) to it.banknote200,
                    Denomination(500, CountryCode.PHP) to it.banknote500,
                    Denomination(1000, CountryCode.PHP) to it.banknote1000
                )
            }.getOrDefault(mapOf())

        Result.success(res)

    } catch (e: Exception) {

        Result.failure(Exception("Couldn't get saved counters from database", e))
    }

    override fun getMostRecentAudit(serialNumber: Long): Result<StateLogEntry> = try {
        val res = store.getMostRecentAudit(serialNumber)
            ?: throw NoSuchElementException()

        Result.success(res)

    } catch (e: Exception) {

        Result.failure(e)
    }

    override fun getMostRecentEmptyTime(serialNumber: Long): Result<Instant> = try {

        val res = store.getMostRecentEmpty(serialNumber)?.time
            ?: throw (NoSuchElementException("No cash collection entry in cashbox log."))

        Result.success(res)

    } catch (e: Exception) {

        Result.failure(e)
    }

    /**
     * Audit the state log.  Function verifies whether the banknote counters that the driver keeps track off
     * match the "stacked" counter that the device keep track off.  The total number of banknotes that
     * have been recorded in between two device counter log entries must match the difference between the
     * two device counter entries.  The device counters should not be reset during the lifetime of the device.
     */
    override suspend fun audit(serialNumber: Long): Result<Boolean> = try {

        var result = true

        var lastDeviceCounter = -1L
        var lastBanknoteCounter = 0
        var totalSinceLastDeviceCounters = 0

        store.getAll(serialNumber).asFlow()
            .collect {

                when (it.event) {

                    "DEVICE_COUNTERS" -> {
                        logger.trace("New Device counters: ${it.stacked}, last device counters: $lastDeviceCounter, total: $totalSinceLastDeviceCounters")

                        if (lastDeviceCounter != -1L) {
                            result = result && (it.stacked - lastDeviceCounter == totalSinceLastDeviceCounters.toLong())
                        }
                        lastDeviceCounter = it.stacked
                        totalSinceLastDeviceCounters = 0
                        lastBanknoteCounter = 0
                    }

                    "EMPTY_CASHBOX" -> lastBanknoteCounter = 0

                    else -> {
                        totalSinceLastDeviceCounters += (it.sumBanknotes - lastBanknoteCounter)
                        logger.trace("Last Device counters: $lastDeviceCounter - lastBanknote: $lastBanknoteCounter - current: ${it.sumBanknotes} - total: $totalSinceLastDeviceCounters")
                        lastBanknoteCounter = it.sumBanknotes
                    }
                }
            }

        Result.success(result)

    } catch (e: Exception) {

        Result.failure(Exception("Failed to perform an audit of the CBA9 State log (not the same as failing the audit)", e))
    }

    override fun close() {}

}