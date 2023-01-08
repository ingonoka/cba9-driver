/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.util

/**
 * Convert a [ByteArray] to a list of Int
 */
fun ByteArray.toListOfInt(): List<Int> = map { it.toInt() }

/**
 * Convert each element of the list to a byte and convert the resulting list of bytes to a byte array.
 */
fun List<Int>.toByteArray(): ByteArray = map { it.toByte() }.toByteArray()