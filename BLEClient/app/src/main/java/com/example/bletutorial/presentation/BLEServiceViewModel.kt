package com.example.bletutorial.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bletutorial.model.domain.ConnectionState
import com.example.bletutorial.model.service.BLEService
import com.example.bletutorial.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BLEServiceViewModel @Inject constructor(
    private val bleService: BLEService
) : ViewModel(){

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
    var sensorState by mutableStateOf<String?>(null)
        private set

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)
    init {
        subscribeToChanges()
    }
    private fun subscribeToChanges(){
        viewModelScope.launch {
            bleService.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        sensorState = result.data.sensors
                    }

                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
    }

    fun disconnect(){
        bleService.disconnect()
    }

    fun reconnect(){
        bleService.reconnect()
    }

    fun initializeConnection(){
        errorMessage = null
        subscribeToChanges()
        bleService.startReceiving()
    }

    override fun onCleared() {
        super.onCleared()
        bleService.closeConnection()
    }

    fun sendCommandToBLEDevice(command: String) {
        bleService.writeCharacteristic(command);
    }

}