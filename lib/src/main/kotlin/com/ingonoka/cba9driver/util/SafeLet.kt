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
 * Function that mimics the functionality of let for multiple parameters.
 *
 * Usage:
 * ```
 * var p1: Int? = null
 * var p2: Int? = null
 * ... Do something to assign p1 and p2 ...
 * safeLet(p1, p2) { runFunction(p1, p2) } ?: throw Exception()
 * ```
 */
inline fun <T1 : Any, T2, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    block: (T1, T2) -> R?
): R? {
    return if (p1 != null && p2 != null) block(
        p1,
        p2
    ) else null
}

/**
 * Function that mimics the functionality of let for multiple parameters.
 *
 * Usage:
 * ```
 * var p1: Int? = null
 * var p2: Int? = null
 * var p3: Int? = null
 * ... Do something to assign p1, p2. ...
 * safeLet(p1, p2, p3) { runFunction(p1, p2, p3) } ?: throw Exception()
 * ```
 */
inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    block: (T1, T2, T3) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null) block(
        p1,
        p2,
        p3
    ) else null
}

/**
 * Function that mimics the functionality of let for multiple parameters.
 *
 * Usage:
 * ```
 * var p1: Int? = null
 * var p2: Int? = null
 * var p3: Int? = null
 * var p4: Int? = null
 * ... Do something to assign p1, p2. ...
 * safeLet(p1, p2, p3, p4) { runFunction(p1, p2, p3, p4) } ?: throw Exception()
 * ```
 */
inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    block: (T1, T2, T3, T4) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(
        p1,
        p2,
        p3,
        p4
    ) else null
}

/**
 * Function that mimics the functionality of let for multiple parameters.
 *
 * Usage:
 * ```
 * var p1: Int? = null
 * var p2: Int? = null
 * var p3: Int? = null
 * var p4: Int? = null
 * var p5: Int? = null
 * ... Do something to assign p1, p2. ...
 * safeLet(p1, p2, p3, p4, p5) { runFunction(p1, p2, p3, p4, p5) } ?: throw Exception()
 * ```
 */
inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    p5: T5?,
    block: (T1, T2, T3, T4, T5) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(
        p1,
        p2,
        p3,
        p4,
        p5
    ) else null
}