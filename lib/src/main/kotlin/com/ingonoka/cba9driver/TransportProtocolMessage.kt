/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver

import com.ingonoka.cba9driver.hexutils.toHexChunked
import com.ingonoka.cba9driver.hexutils.toHexShortShort
import com.ingonoka.cba9driver.util.*

/**
 * Class representing a protocol message for the Smiley Secure Protocol (SSP) of Innovative Technology Ltd
 *
 * STX
 * Single byte indicating the start of a packet, defined as 0x7F. If any other part of the packet contains 0x7F, the
 * last step before transmission the byte should be repeated (0x7F becomes 0x7F, 0x7F) to indicate it is not an STX byte;
 * this is called byte stuffing.
 *
 * SEQ/ID
 * A combination of two items of data: the sequence flag (MSB, bit7) and the address of the device (bit 6 to bit 0, LSB).
 * Each time the master sends a new packet to a device it alternates the sequence flag. If a device receives a packet
 * with the same sequence flag as the last one, it does not execute the command but simply repeats its last reply.
 * In a reply packet the address and sequence flag match the command packet. For example a SMART Hopper by default has
 * an address of 0x10 (16 decimal). When the sync bit is equal to 1 the byte sent to the Hopper is 0x90.
 * On the next command, the sync bit is toggled, in this case 0, the byte sent would be 0x10.
 *
 * Length
 * The number of bytes of data in the data field (including the command and all associated data), it does not include
 * the STX, SEQ/ID, or CRC fields.
 *
 * Data
 * The commands and/or data being sent in the packet to the device.
 *
 * CRC
 * The final 2 bytes are used for a Cyclic Redundancy Check (CRC). This is provided to detect errors during transmission.
 * The CRC is calculated using a forward CRC-16 algorithm with the polynomial (X16 + X15 + X2 + 1). It is calculated on
 * all bytes except STX and initialised using the seed 0xFFFF. The CRC is calculated before byte stuffing.
 */
data class TransportProtocolMessage(
    val sequence: Int,
    val slaveId: Int,
    val data: List<Int>
) : Stringifiable {

    internal val crc: List<Int> by lazy {
        val seqSlaveId = ((sequence shl 7) or slaveId) and 0xFF
        val length = data.size and 0xFF
        crc16(listOf(seqSlaveId, length) + data)
    }

    fun encode(buf: WriteIntBuffer = IntBuffer.empty(), doByteStuffing: Boolean = true): Result<WriteIntBuffer> = try {

        val dataNotStuffed = encode().getOrThrow()

        val dataStuffed = if (doByteStuffing) {
            byteStuffing(dataNotStuffed)
        } else {
            dataNotStuffed
        }

        buf.write(dataStuffed)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    private fun encode(): Result<List<Int>> = try {

        val result = with(IntBuffer.empty()) {
            val useSequence = if (sequence == 0x80) 1 else sequence
            writeByte(0x7F)
            writeByte(((useSequence shl 7) or slaveId).toByte().toInt())
            writeByte(data.size.toByte().toInt())
            write(data)
            write(crc)
            toList()
        }

        Result.success(result)

    } catch (e: Exception) {

        Result.failure(e)
    }

    /**
     * Add stuffing bytes to the encoded message. This ensures that a single 0x7F can only appear at the start of the
     * message sent or returned to/from the device.  In all other places, 0x7F always appears as a pair 0x7f, 0x7f
     */
    internal fun byteStuffing(encodedMsg: List<Int>): List<Int> = encodedMsg.flatMapIndexed { index: Int, byte: Int ->
        if (byte == 0x7F && index > 0)
            listOf(0x7F, 0x7F)
        else
            listOf(byte)
    }

    companion object {

        internal fun decode(bytes: List<Int>): Result<TransportProtocolMessage> = decode(IntBuffer.wrap(bytes))

        internal fun decode(bytes: ReadIntBuffer): Result<TransportProtocolMessage> = try {

            val protocolMessage = deStuffAndDecode(bytes).getOrThrow()

            Result.success(protocolMessage)

        } catch (e: Exception) {
            Result.failure(Exception("Failed build of Protocol Message", e))
        }

        /**
         * Decode a protocol message from a byte array
         */
        private fun deStuffAndDecode(buf: ReadIntBuffer, doByteDeStuffing: Boolean = true): Result<TransportProtocolMessage> =
            try {

                val deStuffed = if (doByteDeStuffing) {
                    IntBuffer.wrap(byteDeStuffing(buf.toList()).getOrThrow())
                } else {
                    buf
                }

                val stx = deStuffed.readByte().getOrThrow()
                if (stx != 0x7F) throw Exception("Expected STX(0x7f), got: $stx")

                val seqSlaveId = deStuffed.readByte().getOrThrow()

                val length = deStuffed.readByte().getOrThrow()

                val data = deStuffed.readList(length).getOrThrow()

                val crc = deStuffed.readList(2).getOrThrow()

                val seq = (seqSlaveId and 0x80) ushr 7
                val slaveId = seqSlaveId and 0x7F

                if (!crc16Validate(listOf(seqSlaveId, length) + data, crc)) throw Exception("CRC incorrect")

                val protocolMessage = TransportProtocolMessage(seq, slaveId, data)

                Result.success(protocolMessage)

            } catch (e: Exception) {

                Result.failure(Exception("Failed reading protocol message from input stream", e))
            }

        /**
         * Remove stuffing bytes from the raw protocol message
         */
        internal fun byteDeStuffing(stuffedMsg: List<Int>): Result<List<Int>> = try {
            val output = if (stuffedMsg.indexOfLast { it == 0x7F } == 0) {
                // Return original message if it starts with a 0x7F and does not contain anymore 0x7F
                stuffedMsg

            } else {
                buildList {
                    add(0x7f)
                    var i = 1
                    var previousWas7f = false
                    while (i < stuffedMsg.size) {
                        previousWas7f = if (stuffedMsg[i] == 0x7f) {
                            if (!previousWas7f) {
                                add(0x7f)
                                true
                            } else {
                                false
                            }
                        } else {
                            if (previousWas7f)
                                throw Exception("Encountered single 7f without additional stuffing")
                            else
                                add(stuffedMsg[i])

                            false
                        }
                        i++
                    }
                    if (previousWas7f) {
                        throw Exception("Encountered single 7f at the of output")
                    }
                    toList()
                }
            }

            Result.success(output)

        } catch (e: Exception) {

            Result.failure(Exception("Failed to de-stuff protocol message", e))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransportProtocolMessage

        if (sequence != other.sequence) return false
        if (slaveId != other.slaveId) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sequence.hashCode()
        result = 31 * result + slaveId
        result = 31 * result + data.hashCode()
        return result
    }

    override fun toString(): String = stringify()

    override fun stringify(short: Boolean, indent: String): String {

        return if (short) {
            "Seq:${if (sequence == 0) 0 else 1},Id:$slaveId,l:${data.size + 3},crc:${crc.toHexShortShort()},d:${data.toHexShortShort()}"

        } else """
            |TransportProtocolMessage:
            |    Sequence:   ${if (sequence == 0) "OFF" else "ON"}
            |    Slave ID:   $slaveId
            |    Length:     ${data.size + 3}
            |    CRC16:      ${crc.toHexShortShort()}
            |    Data:       
            |${data.toHexChunked(indent = "                ", lineNums = false, printable = true)}
            """.trimMargin()
    }
}

