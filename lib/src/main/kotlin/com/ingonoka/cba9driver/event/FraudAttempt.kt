/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

import com.ingonoka.cba9driver.data.Denomination

/**
 * The validator system has detected an attempt to manipulate the coin/banknote in order to fool
 * the system and register credits with no money added.
 *
 * The data byte indicates the dataset channel of the banknote that is being tampered with.
 * A zero indicates that the channel is unknown.
 */
class FraudAttempt(denomination: Denomination) : SspEvent(SspEventCode.FraudAttempt, denomination) {


    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "Fraud - Php $denomination}"
        } else {
            "Fraud Attempt - Php $denomination"
        }
}