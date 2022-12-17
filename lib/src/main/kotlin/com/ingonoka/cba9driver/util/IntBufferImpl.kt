/*
 * Copyright (c) 2021. Ingo Noka
 * This file belongs to project utils-mp.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.util

import java.lang.Integer.max

/**
 * Minimum number of bytes to add to the buffer if capacity is reached when writing data o the buffer.
 */
internal const val MIN_EXTEND_SIZE = 128

interface ReadIntBuffer : IntBuffer {

    /**
     * Read one byte from the buffer.
     */
    fun readByte(): Result<Int>

    /**
     * Read one byte from buffer, but return null instead of failure [Result].
     */
    fun readByteOrNull(): Int?

    /**
     * Read one byte, but do not advance position.
     *
     * ## Usage
     * ```
     * // Read a 3 byte long string, but only if the first byte is not a 0
     * val buf = ListOfIntBuffer.wrap(listOf('A'.code, 'B'.code, 'C'.code))
     * if(buf.peekByte() != 0) {
     *      buf.readString(3) // returns "ABC" wrapped in successful [Result]
     * }
     *
     * ```
     */
    fun peekByte(): Result<Int>

    /**
     * Same as peekByte, but return a null instead of failure [Result]
     */
    fun peekByteOrNull(): Int?
    
    /**
     * Read [n] bytes from the buffer and convert to a [Long] with [byteOrder]
     * ```
     * ```
     * If [byteOrder] is [ByteOrder.BIG_ENDIAN], then the highest byte will be read first
     *
     * ```
     * [0, 0, 0, 0, 0, 0, 0, 1]
     * // will be read as 1
     * ```
     * If [byteOrder] is [ByteOrder.LITTLE_ENDIAN], then the lowest byte will be written first
     *      *
     * ```
     * [1, 0, 0, 0, 0, 0, 0, 0]
     * // will be read as 1
     * ```
     */
    fun readLong(n: Int = 8, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): Result<Long>

    /**
     * Same a [readLong] but returns a null instead of throwing an exception
     */
    fun readLongOrNull(n: Int = 8, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): Long?

    /**
     * Read [Long], but do not advance position.
     *
     */
    fun peekLong(n: Int = 8, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): Result<Long>
    /**
     * Read [Long], but do not advance position. Return null instead of failure [Result]
     *
     */fun peekLongOrNull(n: Int = 8, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): Long?

    /**
     * Read [n] bytes from the buffer and convert to a [Int] with [byteOrder]
     * ```
     * ```
     * If [byteOrder] is [ByteOrder.BIG_ENDIAN], then the highest byte will be read first
     *
     * ```
     * [0, 0, 0, 1]
     * // will be read as 1
     * ```
     * If [byteOrder] is [ByteOrder.LITTLE_ENDIAN], then the lowest byte will be written first
     *      *
     * ```
     * [1, 0, 0, 0]
     * // will be read as 1
     * ```
     */
    fun readInt(n: Int = 4, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): Result<Int>

    /**
     * Same a [readInt] but returns a null instead of throwing an exception
     */
    fun readIntOrNull(n: Int = 4, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): Int?

    /**
     * Read [Int], but do not advance position.
     *
     */
    fun peekInt(n: Int = 4, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): Result<Int>
    fun peekIntOrNull(n: Int = 4, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): Int?

    /**
     * Read [n] bytes and convert to [String] using UTF-8
     */
    fun readString(n: Int): Result<String>

    /**
     * Same as [readString], but return null instead of throwing exception
     */
    fun readStringOrNull(n: Int): String?

    /**
     * Read [String], but do not advance position.
     *
     */
    fun peekString(n: Int): Result<String>
    fun peekStringOrNull(n: Int): String?

    /**
     * Returns a list of bytes, which is a copy of [n] bytes of the buffer starting at [position].
     * * If there are less than [n] bytes left in the backing list returns a Result.Failure.
     * * If [position] is already at  the end of the backing array, or if n is 0 or less, then an empty array is returned.
     */
    fun readList(n: Int = 0): Result<List<Int>>

    /**
     * Same as [readList], but return null instead of throwing exceptions
     */
    fun readListOrNull(n: Int = 0): List<Int>?

    /**
     * Read [List] of bytes, but do not advance position.
     *
     */
    fun peekList(n: Int = 0): Result<List<Int>>
    /**
     * Same as [peekList], but return null instead of throwing exceptions
     */
    fun peekListOrNull(n: Int = 0): List<Int>?

    /**
     * Returns next [n] bytes from buffer as [ByteArray]
     */
    fun readByteArray(n: Int = 0): Result<ByteArray>

    /**
     * Same as [readByteArray], but return null instead of throwing exceptions
     */
    fun readByteArrayOrNull(n: Int = 0): ByteArray?

    /**
     * Read [ByteArray], but do not advance position.
     *
     */
    fun peekByteArray(n: Int = 0): Result<ByteArray>
    /**
     * Same as [peekByteArray], but return null instead of throwing exceptions
     */
    fun peekByteArrayOrNull(n: Int = 0): ByteArray?

    /**
     * Reads the remaining bytes. returns an empty list when no bytes are left to read.
     */
    fun readRemaining(): Result<List<Int>>

    /**
     * Same as [readRemaining], but return null instead of throwing exceptions
     */
    fun readRemainingOrNull(): List<Int>?

    /**
     * Read remaining bytes, but do not advance position.
     *
     */
    fun peekRemaining(): Result<List<Int>>
    /**
     * Same as [peekRemaining], but return null instead of throwing exceptions
     */
    fun peekRemainingOrNull(): List<Int>?

    /**
     * Return true if number of bytes left to read is at least [n]
     */
    fun hasBytesLeftToRead(n: Int = 1): Boolean

    /**
     * Number of bytes still left to read.
     */
    fun bytesLeftToRead(): Int

    /**
     * Set [position] to 0.  After calling this function, the buffer can be read from the start again.
     */
    fun rewind(): ReadIntBuffer

    /**
     * The number of bytes that can be read from the buffer
     */
    val watermark: Int
}

interface WriteIntBuffer : IntBuffer {

    /**
     * Copy a single byte [b] into the buffer
     *
     * @throws IllegalArgumentException If [b] is not in -128..127
     */
    fun writeByte(b: Int)

    /**
     * Copy [l] into buffer (n bytes only)
     * ```
     * ```
     * If [byteOrder] is [ByteOrder.BIG_ENDIAN], then the highest byte will be written first
     *
     * ```
     * // 1 is written as
     * [0, 0, 0, 0, 0, 0, 0, 1]
     * ```
     * If [byteOrder] is [ByteOrder.LITTLE_ENDIAN], then the lowest byte will be written first
     *      *
     * ```
     * // 1 is written as
     * [1, 0, 0, 0, 0, 0, 0, 0]
     * ```
     * @param n Number of bytes to write
     */
    fun write(l: Long, n: Int = 8, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN)

    /**
     * Copy [i] into buffer (n bytes only)
     * ```
     * ```
     * If [byteOrder] is [ByteOrder.BIG_ENDIAN], then the highest byte will be written first
     *
     * ```
     * // 1 is written as
     * [0, 0, 0, 1]
     * ```
     * If [byteOrder] is [ByteOrder.LITTLE_ENDIAN], then the lowest byte will be written first
     *      *
     * ```
     * // 1 is written as
     * [1, 0, 0, 0]
     * ```
     * @param n Number of bytes to write
     */
    fun write(i: Int, n: Int = 4, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN)

    /**
     * Write st[str]ring in UTF-8 encoding to buffer
     */
    fun write(str: String)

    /**
     * Write [arr] to buffer
     */
    fun write(arr: ByteArray)

    /**
     * Write [list] to buffer
     */
    fun write(list: List<Int>)

    /**
     * Current size of the backing buffer, i.e. the total number of bytes that can be written without triggering an
     * extension (and copy) of the backing buffer
     */
    val capacity: Int

    /**
     * Reset the buffer, which allows reuse of the buffer. This is equivalent to deleting the content of the buffer.
     * New writes will start at [position] 0.
     */
    fun reset()

    /**
     * Create a [ReadIntBuffer] from this [WriteIntBuffer]. The [position] will be 0, and the
     * [ReadIntBuffer.watermark] is the position before calling this function.  Effectively allows reading the data that was
     * written to the buffer.
     */
    fun toReadListOfIntBuffer(): ReadIntBuffer
}

interface IntBuffer {
    /**
     * Current read/write position in the buffer
     */
    val position: Int

    /**
     * Return the entire buffer content up to but excluding the current position
     */
    fun toList(): List<Int>

    companion object {

        /**
         * Wrap a list of integers into a [ReadIntBuffer], ready for reading at [position] 0
         *
         * @param n The number of bytes that are actually populated in [buf]. Must be positive and not bigger than the size of [buf].
         *
         */
        fun wrap(buf: List<Int>, n: Int = buf.size): ReadIntBuffer {
            require(buf.all { it in Byte.MIN_VALUE..Byte.MAX_VALUE })
            require(n in 0..buf.size)
            return IntBufferImpl(buf.size, buf.toMutableList(), n)
        }

        /**
         * Wrap a list of integers into a [ReadIntBuffer], ready for reading at [position] 0
         */
        fun wrap(vararg ints: Int): ReadIntBuffer = wrap(ints.asList())

        /**
         * Create an empty [WriteIntBuffer], ready for writing at [position] 0
         */
        fun empty(n: Int = MIN_EXTEND_SIZE): WriteIntBuffer = IntBufferImpl(n)
    }
}

/**
 * Buffer that keeps a read/write position and is backed by a list of integers.  Each integer
 * represents a byte from -128 to 127
 * ```
 * ```
 * ## Create a [IntBufferImpl] For Reading
 * ```
 * // from a list of integers
 * val readBuffer = ListOfIntBuffer.wrap(listOfInt)
 * // from a write buffer
 * val readBuffer = writeBuffer.toReadListOfIntBuffer()
 * ```
 * ## Create a [IntBufferImpl] for writing
 * ```
 * val writeBuffer = ListOfIntBuffer.empty(n)
 * ```
 * ## Usage
 *
 * ```
 * writeBuffer.write(1) // Integer (4 bytes)
 * writeBuffer.write(1L) // Long (8 bytes)
 * // etc., see write functions for details
 *
 * val i = readBuffer.readInt()
 * val l = readBuffer.readLong()
 * // etc., see read functions for details
 * ```
 */
class IntBufferImpl internal constructor(
    private val n: Int,
    /**
     * The backing buffer
     */
    private var buffer: MutableList<Int> = MutableList(n) { 0 },
    override var watermark: Int = 0
) : ReadIntBuffer, WriteIntBuffer {

    override var position = 0
        private set

    override val capacity
        get() = buffer.size
    
    override fun writeByte(b: Int) {
        require(b in Byte.MIN_VALUE.toInt()..Byte.MAX_VALUE.toInt())
        if (position + 1 > buffer.size) extend(position + MIN_EXTEND_SIZE)
        buffer[position++] = b
    }

    override fun readByte(): Result<Int> {
        return when {
            hasBytesLeftToRead() -> Result.success(buffer[position++])
            else -> Result.failure(Exception("Cannot read from position $position.  Buffer size: ${buffer.size}"))
        }
    }

    override fun readByteOrNull(): Int? = readByte().getOrNull()

    override fun peekByte(): Result<Int> {
        val pos = position
        val b = readByte()
        position = pos
        return b
    }

    override fun peekByteOrNull(): Int? {
        val pos = position
        val b = readByteOrNull()
        position = pos
        return b
    }
    
    override fun write(l: Long, n: Int, byteOrder: ByteOrder) {

        when (n) {
            1 -> require(l in 0..0xFF)
            2 -> require(l in 0..0xFFFF)
            3 -> require(l in 0..0xFFFFFF)
            4 -> require(l in 0..0xFFFFFFFF)
            5 -> require(l in 0..0xFFFFFFFFFF)
            6 -> require(l in 0..0xFFFFFFFFFFFF)
            7 -> require(l in 0..0xFFFFFFFFFFFFFF)
        }

        if (position + n > buffer.size) extend(position + max(8, MIN_EXTEND_SIZE))

        val buf = when (n) {

            7 -> listOf(
                ((l shr 48) and 255).toByte(),
                ((l shr 40) and 255).toByte(),
                ((l shr 32) and 255).toByte(),
                ((l shr 24) and 255).toByte(),
                ((l shr 16) and 255).toByte(),
                ((l shr 8) and 255).toByte(),
                (l and 255).toByte()
            )

            6 -> listOf(
                ((l shr 40) and 255).toByte(),
                ((l shr 32) and 255).toByte(),
                ((l shr 24) and 255).toByte(),
                ((l shr 16) and 255).toByte(),
                ((l shr 8) and 255).toByte(),
                (l and 255).toByte()
            )

            5 -> listOf(
                ((l shr 32) and 255).toByte(),
                ((l shr 24) and 255).toByte(),
                ((l shr 16) and 255).toByte(),
                ((l shr 8) and 255).toByte(),
                (l and 255).toByte()
            )

            4 -> listOf(
                ((l shr 24) and 255).toByte(),
                ((l shr 16) and 255).toByte(),
                ((l shr 8) and 255).toByte(),
                (l and 255).toByte()
            )

            3 -> listOf(
                ((l shr 16) and 255).toByte(),
                ((l shr 8) and 255).toByte(),
                (l and 255).toByte()
            )

            2 -> listOf(
                ((l shr 8) and 255).toByte(),
                (l and 255).toByte()
            )

            1 -> listOf(
                (l and 255).toByte()
            )

            else -> listOf(
                ((l shr 56) and 255).toByte(),
                ((l shr 48) and 255).toByte(),
                ((l shr 40) and 255).toByte(),
                ((l shr 32) and 255).toByte(),
                ((l shr 24) and 255).toByte(),
                ((l shr 16) and 255).toByte(),
                ((l shr 8) and 255).toByte(),
                (l and 255).toByte()
            )
        }.map { it.toInt() }

        write(if (byteOrder == ByteOrder.LITTLE_ENDIAN) buf.asReversed() else buf)
    }

    override fun readLong(n: Int, byteOrder: ByteOrder): Result<Long> = try {
        require(position + n <= watermark)
        require(n in 0..8) { "Can only read Long values that are 1 to 8 bytes long: $n" }

        val l = readList(n)
            .map { if (byteOrder == ByteOrder.LITTLE_ENDIAN) it.reversed() else it }
            .mapCatching { it.toLongNoLeadingZeros(n).getOrThrow() }
            .getOrThrow()

        Result.success(l)

    } catch (e: Exception) {

        Result.failure(e)
    }

    override fun readLongOrNull(n: Int, byteOrder: ByteOrder): Long? = readLong(n, byteOrder).getOrNull()

    override fun peekLong(n: Int, byteOrder: ByteOrder): Result<Long> {
        val pos = position
        val l = readLong()
        position = pos
        return l
    }

    override fun peekLongOrNull(n: Int, byteOrder: ByteOrder): Long? {
        val pos = position
        val l = readLongOrNull()
        position = pos
        return l    
    }
    
    override fun write(i: Int, n: Int, byteOrder: ByteOrder) {

        when (n) {
            1 -> require(i in 0..0xFF)
            2 -> require(i in 0..0xFFFF)
            3 -> require(i in 0..0xFFFFFF)
        }

        if (position + n > buffer.size) extend(position + max(4, MIN_EXTEND_SIZE))

        val buf = when (n) {

            3 -> listOf(
                ((i shr 16) and 255).toByte(),
                ((i shr 8) and 255).toByte(),
                (i and 255).toByte()
            )

            2 -> listOf(
                ((i shr 8) and 255).toByte(),
                (i and 255).toByte()
            )

            1 -> listOf(
                (i and 255).toByte()
            )

            else -> listOf(
                ((i shr 24) and 255).toByte(),
                ((i shr 16) and 255).toByte(),
                ((i shr 8) and 255).toByte(),
                (i and 255).toByte()
            )
        }.map { it.toInt() }

        write(if (byteOrder == ByteOrder.LITTLE_ENDIAN) buf.asReversed() else buf)
    }

    override fun readInt(n: Int, byteOrder: ByteOrder): Result<Int> = try {
        require(position + n <= watermark)
        require(n in 1..4)

        val l = readList(n)

        val i = l.map { if (byteOrder == ByteOrder.LITTLE_ENDIAN) it.reversed() else it }
            .mapCatching { it.toIntNoLeadingZeros(n).getOrThrow() }
            .getOrThrow()

        Result.success(i)

    } catch (e: Exception) {

        Result.failure(e)
    }

    override fun readIntOrNull(n: Int, byteOrder: ByteOrder): Int? = readInt(n, byteOrder).getOrNull()

    override fun peekInt(n: Int, byteOrder: ByteOrder): Result<Int> {
        val pos = position
        val i = readInt()
        position = pos
        return i
    }

    override fun peekIntOrNull(n: Int, byteOrder: ByteOrder): Int? {
        val pos = position
        val l = readIntOrNull()
        position = pos
        return l
    }
    
    override fun write(str: String) {
        val list = str.encodeToByteArray().toListOfInt()
        write(list)
    }

    override fun readString(n: Int): Result<String> = readList(n).mapCatching { it.toByteArray().toString(Charsets.UTF_8) }

    override fun readStringOrNull(n: Int): String? = readString(n).getOrNull()

    override fun peekString(n: Int): Result<String> {
        val pos = position
        val s = readString(n)
        position = pos
        return s
    }

    override fun peekStringOrNull(n: Int): String? {
        val pos = position
        val s = readStringOrNull(n)
        position = pos
        return s
    }
    
    override fun write(arr: ByteArray) = write(arr.toListOfInt())

    override fun readByteArray(n: Int): Result<ByteArray> = readList(n).mapCatching { it.toByteArray() }

    override fun readByteArrayOrNull(n: Int): ByteArray? = readList(n).mapCatching { it.toByteArray() }.getOrNull()

    override fun peekByteArray(n: Int): Result<ByteArray> {
        val pos = position
        val ba = readByteArray(n)
        position = pos
        return ba
    }

    override fun peekByteArrayOrNull(n: Int): ByteArray? {
        val pos = position
        val ba = readByteArrayOrNull(n)
        position = pos
        return ba
    }
    
    override fun write(list: List<Int>) {
        if (position + list.size > buffer.size) extend(position + max(list.size, MIN_EXTEND_SIZE))
        list.copyInto(
            srcPos = 0,
            dest = buffer,
            destPos = position,
            length = list.size
        )
        position += list.size
    }

    override fun readList(n: Int): Result<List<Int>> = try {

        if (n > bytesLeftToRead()) throw IndexOutOfBoundsException()

        val l = buffer.slice(position until position + n)
        position += n

        Result.success(l)

    } catch (e: Exception) {

        Result.failure(e)
    }

    override fun readListOrNull(n: Int): List<Int>? = readList(n).getOrNull()

    override fun peekList(n: Int): Result<List<Int>> {
        val pos = position
        val l = readList(n)
        position = pos
        return l
    }

    override fun peekListOrNull(n: Int): List<Int>? {
        val pos = position
        val l = readListOrNull(n)
        position = pos
        return l
    }
    
    override fun readRemaining(): Result<List<Int>> {
        val list = if (bytesLeftToRead() == 0) {
            listOf()
        } else {
            buffer.slice(position until watermark)
        }
        position = watermark
        return Result.success(list)
    }

    override fun readRemainingOrNull(): List<Int>? = readRemaining().getOrNull()

    override fun peekRemaining(): Result<List<Int>> {
        val pos = position
        val r = readRemaining()
        position = pos
        return r
    }

    override fun peekRemainingOrNull(): List<Int>? {
        val pos = position
        val r = readRemainingOrNull()
        position = pos
        return r
    }

    override fun toList(): List<Int> = buffer.take(max(position, watermark))

    override fun hasBytesLeftToRead(n: Int): Boolean = bytesLeftToRead() >= n
    override fun bytesLeftToRead(): Int = watermark - position

    override fun toReadListOfIntBuffer(): ReadIntBuffer {
        val readBuffer = IntBufferImpl(this.buffer.size, this.buffer)
        readBuffer.watermark = position
        readBuffer.position = 0
        return readBuffer
    }

    override fun rewind(): ReadIntBuffer {
        position = 0
        return this
    }

    override fun reset() {
        watermark = 0
        position = 0
    }

    /**
     * Create new backing buffer, which is n bytes larger than the current one. The content of the
     * original buffer is copied into the new one.
     */
    private fun extend(n: Int) {
        buffer = (buffer + List(n) { 0 }).toMutableList()
    }

    /**
     * Hash code based on populated part of backing list, i.e. the first [position] bytes
     */
    override fun hashCode(): Int = buffer.take(max(position, watermark)).fold(1) { acc, element ->
        acc * 31 + element
    }

    /**
     * Two buffers are equal if [position], [watermark] and the populated bytes are the same
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as IntBufferImpl

        if (n != other.n) return false
        if (position != other.position) return false
        if (watermark != other.watermark) return false
        val bytesToCompare = max(position, watermark)
        if (buffer.take(bytesToCompare) != other.buffer.take(bytesToCompare)) return false

        return true
    }
}

fun List<Int>.listOfIntBuffer() = IntBuffer.wrap(this)