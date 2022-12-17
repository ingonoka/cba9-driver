/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.TransportProtocolMessage
import com.ingonoka.cba9driver.hexutils.toHexShortShort
import com.ingonoka.cba9driver.response.SspResponse
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

/**
 * The SSP command always has a command code [sspCommandCode] and some commands have extra [data].
 *
 */
sealed class SspCommand(
    val sspCommandCode: SspCommandCode,
    val data: List<Int> = listOf()
) : Stringifiable {

    val logger = LoggerFactory.getLogger(this::class.java.name)

    companion object {
        /**
         * "Sequence bit" for the SSP protocol messages. Alternates between 1 and 0
         */
        var sequence: Int = 1

        /**
         * Holding the Mutex will ensure that function [run] is run only once at a time.
         */
        val executionMutex = Mutex(false)
    }

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<List<Int>> = try {
        sspCommandCode.encode(buf)
        buf.write(data)
        Result.success(buf.toList())

    } catch (e: Exception) {

        Result.failure(e)
    }

    /**
     * Check whether this command hasn't been executed and execute it if it hasn't.
     *
     * @return [SspCommand] Wrapped in [Result]
     * @throws RuntimeException if the command had already been executed
     */
    suspend fun run(transceiver: IUsbTransceiver): Result<SspResponse> = try {

        executionMutex.withLock {

            val data = encode().getOrThrow()

            val commandProtocolMessage = TransportProtocolMessage(sequence, 0, data)
                .encode(IntBuffer.empty())
                .map { it.toList() }
                .getOrThrow()

            val received = transceiver.transceive(commandProtocolMessage).getOrThrow()

            val responseProtocolMessage = TransportProtocolMessage.decode(IntBuffer.wrap(received)).getOrThrow()

            if (sspCommandCode != SspCommandCode.Sync && responseProtocolMessage.sequence != sequence)
                throw Exception("Sequence bit out of sync. Expected: $sequence, actual: ${responseProtocolMessage.sequence}")

            sequence = sequence xor 1

            SspResponse.decode(responseProtocolMessage.data)

        }.onSuccess {

            logger.trace("Run ${this.stringify()} -> ${it.stringify()}")
        }

    } catch (e: Exception) {

        Result.failure(Exception("Failed transceive for: ${this.sspCommandCode}", e))
    }

    override fun stringify(short: Boolean, indent: String): String = if (short) {
        indent + "Cmd: $sspCommandCode ${if (data.isNotEmpty()) ": ${data.toHexShortShort()}" else ""}"
    } else {
        """|
            |${indent}Command:      ${sspCommandCode.stringify()}
            |${indent}Data:         ${if (data.isNotEmpty()) ": ${data.toHexShortShort()}" else "NONE"}
        """.trimMargin()
    }
}