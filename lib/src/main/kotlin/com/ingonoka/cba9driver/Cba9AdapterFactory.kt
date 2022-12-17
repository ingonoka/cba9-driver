/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver

import com.ingonoka.cba9driver.statelog.ICba9StateLog
import com.ingonoka.cba9driver.statelog.inMemoryCba9StateLog
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.usbmanager.UsbDeviceAdapter

/**
 * CBA9 specific configuration names
tag::cba9-specific-properties[]
[cols="30,30,40"]
|====
|Property name|Format|Usage

|config.cba9.protocolVersion
|Decimal digit. Default `7`
|Protocol version that will be set with the "Host Protocol Version" command.

|config.cba9.acceptedDenominations
|String with format `<Currency><Amount>`.  For example: "PHP20,PHP50,PHP100" Default is all available denominations
for the BNA
|Will be converted to a list of channels and used by the "Set Inhibits" command.

|config.cba9.cashboxCapacity
|Number. Default "300".
|Number of banknotes that fit into the cashbox of the BNA

|config.cba9.pollInterval
| Number. Default "1000"
| Number of milliseconds between poll commands.

|config.cba9.holdInterval
| Number. Default "4500"
| Number of milliseconds between hold commands.

|config.cba9.cashboxMaxLoadBetweenCollections
| Amount in Pesos. Default "300000"
| Maximum amount that can be loaded in between cash collections

| config.cba9.resetOnAttachment
| Boolean
| The driver will send a reset to the device on the first attachment attempt. Can be used to get the device out of a
deadlock, but should be false in normal operation.


|====
end::cba9-specific-properties[]

tag::cba9-default-configuration[]
[cols="40,60"]
|====
|NAMES|Cba9,cba9,CBA9,CBA9SP
|VENDOR_ID| 6428
|PRODUCT_ID| 16644
|SENDING_ENDPOINT_INDEX| 1
|RECEIVING_ENDPOINT_INDEX| 2
|INTERFACE_INDEX| 1
|TIMEOUT_SEND| 2000 ms
|TIMEOUT_RECEIVE| 2000 ms
|TRANSFER_MODE| BULK_TIMEOUT

|CBA9_PROTOCOL_VERSION| 7
|CBA9_ACCEPTED_DENOMINATIONS| PHP20,PHP50,PHP100,PHP200,PHP500,PHP1000
|CBA9_CASHBOX_CAPACITY| 300
|CONFIG_CBA9_POLL_INTERVAL| 1000 ms
|CONFIG_CBA9_HOLD_INTERVAL| 4500 ms
|CONFIG_CBA9_CASHBOX_MAX_LOAD_BETWEEN_COLLECTIONS| PHP 300,000
|CONFIG_CBA9_RESET_ON_ATTACHMENT|false
|====

end::cba9-default-configuration[]

 */


/**
 * Factory creates new driver instances for CBA9 devices
 */
class Cba9AdapterFactory : Stringifiable {

    fun createCba9(usbDeviceAdapter: UsbDeviceAdapter): Result<Cba9> = try {

        Result.success(Cba9(cba9Props, stateLog, usbDeviceAdapter))

    } catch (e: Exception) {

        Result.failure(e)
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "USB Adapter for CBA9"
        } else {
            "USB Adapter for CBA9/Banknote Acceptor/Innovative Technology Ltd/Vendor ID 6428/Product ID 16644"
        }

    companion object {

        private var stateLog: ICba9StateLog = inMemoryCba9StateLog()

        private var cba9Props = Cba9Properties()
    }
}