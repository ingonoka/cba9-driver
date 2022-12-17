/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

/**
 * A bill has been detected as jammed during it's transport through the validator.
 * An unsafe jam indicates that this bill may be in a position when the user could retrieve it
 * from the validator bezel.
 */
class UnsafeJam : SspEvent(SspEventCode.UnsafeJam)