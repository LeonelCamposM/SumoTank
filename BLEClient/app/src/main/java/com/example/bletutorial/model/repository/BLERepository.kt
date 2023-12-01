package com.example.bletutorial.model.repository

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.example.bletutorial.model.data.BLEResult
import com.example.bletutorial.model.domain.ConnectionState
import com.example.bletutorial.model.service.BLEService
import com.example.bletutorial.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BLERepository @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : BLEService {
    private val deviceName = "BLE TANK"
    private val tankServiceUUID  = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    private val tankControlUUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    private val tankSensorUUID = "12345678-1234-1234-1234-123456789abc"

    override val data: MutableSharedFlow<Resource<BLEResult>> = MutableSharedFlow()
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
    private var gatt: BluetoothGatt? = null
    private var isScanning = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    enum class ResourceStatus {
        SUCCESS, ERROR, LOADING
    }

    init {
        coroutineScope.launch {
            data.emit(Resource.Success(data = BLEResult(ConnectionState.Uninitialized, "")))
        }
    }

    private fun emitResult(
        status: ResourceStatus,
        data: BLEResult? = null,
        message: String? = null
    ) {
        coroutineScope.launch {
            val resource = when (status) {
                ResourceStatus.SUCCESS -> Resource.Success(data ?: BLEResult(
                    ConnectionState.Disconnected, ""
                ))
                ResourceStatus.ERROR -> Resource.Error(message ?: "Unknown error")
                ResourceStatus.LOADING -> Resource.Loading(data, message)
            }
            this@BLERepository.data.emit(resource)
        }
    }

    private val scanCallback = object : ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if(result.device.name == deviceName){
                emitResult(ResourceStatus.LOADING, message = "Connecting to device...")
                if(isScanning){
                    result.device.connectGatt(context,false, gattCallback)
                    isScanning = false
                    bleScanner.stopScan(this)
                }
            }
        }
    }

    private var currentConnectionAttempt = 1
    private var maximumConnectionAttempts = 5
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                val data = characteristic.value
                val dataString = data.toString(Charsets.UTF_8)
                emitResult(ResourceStatus.SUCCESS, BLEResult(ConnectionState.Connected, dataString))
            } else {
                emitResult(ResourceStatus.ERROR, message = "Read characteristic failed")
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when {
                status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED -> handleConnectionSuccess(
                    gatt
                )

                newState == BluetoothProfile.STATE_DISCONNECTED -> handleDisconnection()
                else -> handleConnectionFailure(gatt)
            }
        }

        private fun handleConnectionSuccess(gatt: BluetoothGatt) {
            emitResult(ResourceStatus.SUCCESS, BLEResult(ConnectionState.Connected, ""))
            gatt.discoverServices()
            this@BLERepository.gatt = gatt
        }

        private fun handleDisconnection() {
            emitResult(ResourceStatus.SUCCESS, BLEResult(ConnectionState.Disconnected, ""))
            gatt?.close()
            gatt = null
        }

        private fun handleConnectionFailure(gatt: BluetoothGatt) {
            gatt.close()
            currentConnectionAttempt += 1
            if (currentConnectionAttempt <= maximumConnectionAttempts) {
                retryConnection()
            } else {
                emitResult(ResourceStatus.ERROR, message = "Could not connect to ble device")
            }
        }

        private fun retryConnection() {
            emitResult(ResourceStatus.LOADING, message = "Connecting to device...")
            startReceiving()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d("BLERepository", "Services Discovered: Status=$status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleSuccessfulServiceDiscovery(gatt)
            } else {
                emitResult(ResourceStatus.ERROR, message = "Service discovery failed")
                Log.e("BLERepository", "Service Discovery Failed")
            }
        }


        private fun handleSuccessfulServiceDiscovery(gatt: BluetoothGatt) {
            gatt.services.forEach { service ->
                Log.d("BLERepository", "Discovered service: ${service.uuid}")
                service.characteristics.forEach { characteristic ->
                    Log.d("BLERepository", "Characteristic: ${characteristic.uuid}")
                }
            }
            val characteristic = findCharacteristics(tankServiceUUID, tankControlUUID)
            if (characteristic != null) {
                emitResult(ResourceStatus.SUCCESS, BLEResult(ConnectionState.Connected, ""))
            } else {
                emitResult(ResourceStatus.ERROR, message = "Control characteristic not found")
            }
            val characteristicN = findCharacteristics(tankServiceUUID, tankSensorUUID)
            if (characteristicN != null) {
                 Log.d("BLERepository", "Discovered characteristicN: ${tankSensorUUID}")
            } else {
                emitResult(ResourceStatus.ERROR, message = "sensor characteristic not found")
            }
        }
    }


    override fun writeCharacteristic(command: String) {
        val characteristic = findCharacteristics(tankServiceUUID, tankControlUUID)
        characteristic?.let { char ->
            char.value = command.toByteArray(Charsets.UTF_8)
            gatt?.writeCharacteristic(char)
        }
    }

    override fun readSensorCharacteristic() {
        val characteristic = findCharacteristics(tankServiceUUID, tankSensorUUID)
        characteristic?.let {
            gatt?.readCharacteristic(it)
        } ?: emitResult(ResourceStatus.ERROR, message = "Sensor characteristic not found")
    }

    private fun findCharacteristics(serviceUUID: String, characteristicsUUID:String):BluetoothGattCharacteristic?{
        return gatt?.services?.find { service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristics ->
            characteristics.uuid.toString() == characteristicsUUID
        }
    }

    override fun startReceiving() {
        emitResult(ResourceStatus.LOADING, message = "Scanning  devices...")
        isScanning = true
        bleScanner.startScan(null,scanSettings,scanCallback)
    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }
}