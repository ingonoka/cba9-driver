/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

/**
 * A command that behaves in the same way as the Poll command but with this command, some events will need to be
 * acknowledged by the host using the EVENT ACK command (0x56). See the description of individual events to find
 * out if they require acknowledgement. If there is an event that requires acknowledgement the response will not
 * change until the EVENT ACK command is sent and the BNV will not allow any further note actions until the event
 * has been cleared by the EVENT ACK command. If this command is not supported by the slave device, then generic
 * response 0xF2 will be returned and standard poll command (0x07) will have to be used.
 */
class PollWithAck : SspCommand(SspCommandCode.PollWithAck) {

    fun execute(): Result<SspCommand> {
        TODO()
    }
}