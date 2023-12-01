package com.example.bletutorial.model.service

import com.example.bletutorial.model.data.BLEResult
import com.example.bletutorial.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface BLEService {

    val data: MutableSharedFlow<Resource<BLEResult>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()
    fun writeCharacteristic(command: String)
}