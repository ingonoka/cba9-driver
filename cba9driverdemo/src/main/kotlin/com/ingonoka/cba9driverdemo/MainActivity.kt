/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driverdemo

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ingonoka.cba9driver.Cba9
import com.ingonoka.cba9driver.Cba9Factory
import com.ingonoka.cba9driver.data.CountryCode
import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.statelog.roomCba9StateLog
import com.ingonoka.cba9driverdemo.databinding.ActivityMainBinding
import com.ingonoka.usbmanager.DriverAttachmentEvent
import com.ingonoka.usbmanager.UsbDeviceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // tag::cba9factory-config[]
        /**
         * Example for changing state log for newly created driver to a Room
         * based log
         */
        Cba9Factory.stateLog = roomCba9StateLog(this)

        /**
         * Example for changing properties of CBA9 driver to limiting accepted
         * banknotes to 20 and 100 Pesos
         */
        Cba9Factory.cba9Props = Cba9Factory.cba9Props.copy(
            acceptedDenominations = listOf(
                Denomination(20, CountryCode.PHP),
                Denomination(100, CountryCode.PHP)
            )
        )

        /**
         * Example for changing USB properties for newly created CBA9 driver
         */
        Cba9Factory.cba9Props = Cba9Factory.cba9Props.copy(
            usbProps = Cba9Factory.cba9Props.usbProps.copy(
                timeoutReceive = 3.seconds
            )
        )

        // end::cba9factory-config[]

        // tag::usbDeviceManager[]
        val usbManager = UsbDeviceManager()
        usbManager.start(this)
        // end::usbDeviceManager[]


        monitorCba9Connection(usbManager)

        monitorConnectedDrivers(usbManager, binding)

        binding.buttonAcceptBanknote.setOnClickListener {
            usbManager.getDriver(Cba9::class).onSuccess {
                it.cba9Validator.value?.acceptBanknote()
            }
        }

        binding.buttonRejectBanknote.setOnClickListener {
            usbManager.getDriver(Cba9::class).onSuccess {
                it.cba9Validator.value?.rejectBanknote()
            }
        }

    }

    // tag::monitor-connected-drivers[]
    private fun monitorConnectedDrivers(usbManager: UsbDeviceManager, binding: ActivityMainBinding) =

        lifecycleScope.launch {
            var configUpdater: Job? = null
            var statusUpdater: Job? = null

            usbManager.connectedDrivers.collect { drivers ->
                if (drivers.isNotEmpty()) {
                    logger.info("Connected drivers: ${drivers.joinToString { it.preferredName }}")

                    usbManager.getDriver(Cba9::class)
                        .onSuccess { configUpdater = updateCba9ConfigData(it, binding) }
                    usbManager.getDriver(Cba9::class)
                        .onSuccess { statusUpdater = updateCba9Status(it, binding) }

                } else {

                    configUpdater?.cancelAndJoin()
                    statusUpdater?.cancelAndJoin()
                }
            }
        }
    // end::monitor-connected-drivers[]

    private fun updateCba9ConfigData(cba9: Cba9, binding: ActivityMainBinding) = lifecycleScope.launch {

        try {
            cba9.cba9Validator.filterNotNull().collect {
                binding.textViewProtocolVersionValue.text = it.configData.protocolVersion.toString()
                binding.textViewSerialNumberValue.text = it.serialNumber.value.toString()
                binding.textViewDataSetVersionValue.text = it.datasetVersion.datasetVersion
            }
        } finally {
            binding.textViewProtocolVersionValue.text = getString(R.string.na)
            binding.textViewSerialNumberValue.text = getString(R.string.na)
            binding.textViewDataSetVersionValue.text = getString(R.string.na)
        }
    }

    private fun updateCba9Status(cba9: Cba9, binding: ActivityMainBinding) = lifecycleScope.launch {

        try {
            cba9.cba9Validator.filterNotNull().collect { iCba9Validator ->
                iCba9Validator.state.collect { stateHolder ->
                    binding.textViewCba9StateValue.text = stateHolder.stringify()
                    binding.textViewBanknote.text = if (stateHolder.denomination.countryCode == CountryCode.UNKNOWN) {
                        "${cba9.cba9Validator.value?.configData?.countryCode ?: getString(R.string.na)}\n0"
                    } else {
                        "${stateHolder.denomination.countryCode}\n${stateHolder.denomination.denomination}"
                    }
                }
            }
        } finally {
            binding.textViewCba9StateValue.text = getString(R.string.na)
            binding.textViewBanknote.text = "${cba9.cba9Validator.value?.configData?.countryCode ?: getString(R.string.na)}"

        }
    }

    // tag::fillLevel[]
    private fun updateCba9FillLevel(cba9: Cba9, binding: ActivityMainBinding) =
        lifecycleScope.launch {

        cba9.cba9Validator.filterNotNull().collect {
            val currency = it.configData.countryCode

            it.cashbox.levels.collect { levelHolder ->

                binding.textViewFillLevelValue.text =
                    getString(
                        R.string.fillLevel,
                        currency.countryCode,
                        levelHolder.banknoteValue,
                        levelHolder.banknoteCount
                    )
            }
        }
    }
    // end::fillLevel[]


    private fun monitorCba9Connection(usbManager: UsbDeviceManager) = lifecycleScope.launch {


        usbManager.getAttachmentEvents<Cba9>().collect { event ->

            when (event) {
                is DriverAttachmentEvent.Attached -> logger.info(event.toString())
                is DriverAttachmentEvent.Attaching -> logger.info(event.toString())
                is DriverAttachmentEvent.Detached -> logger.info(event.toString())
                DriverAttachmentEvent.Empty -> logger.info(event.toString())
                is DriverAttachmentEvent.Failed -> logger.warn(event.toString())
            }
        }

    }
}