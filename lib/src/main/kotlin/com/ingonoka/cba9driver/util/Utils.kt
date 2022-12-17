/*
 * Copyright (c) 2021. Ingo Noka
 * This file belongs to project utils-mp.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.util

/**
 * Collect the messages from a stack trace of a [Throwable] into a list of strings
 */
fun Throwable.collectExceptionMessages(): List<String> =

    with(mutableListOf<String>()) {
        var exception: Throwable? = this@collectExceptionMessages
        do {
            exception?.message?.let { add(it) } ?: add("No message")
            exception = exception?.cause
        } while (exception != null)
        toList()
    }

/**
 * Concatenate messages from the call stack of a [Throwable] separated by [separator] and return as string
 */
fun Throwable.combineAllMessages(separator: String): String = collectExceptionMessages().joinToString(separator)


/**
 * Return the receiver if it is of type [R], or null otherwise
 */
inline fun <reified R> Any?.takeIfIsInstance(): R? = if (this is R) this else null

