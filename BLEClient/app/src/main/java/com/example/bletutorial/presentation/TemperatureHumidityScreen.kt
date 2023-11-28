package com.example.bletutorial.presentation

import android.bluetooth.BluetoothAdapter
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bletutorial.data.ConnectionState
import com.example.bletutorial.presentation.permissions.PermissionUtils
import com.example.bletutorial.presentation.permissions.SystemBroadcastReceiver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TemperatureHumidityScreen(
    onBluetoothStateChanged:()->Unit,
    viewModel: TempHumidityViewModel = hiltViewModel()
) {

    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED){ bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if(action == BluetoothAdapter.ACTION_STATE_CHANGED){
            onBluetoothStateChanged()
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)
    val lifecycleOwner = LocalLifecycleOwner.current
    val bleConnectionState = viewModel.connectionState

    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver{_,event ->
                if(event == Lifecycle.Event.ON_START){
                    permissionState.launchMultiplePermissionRequest()
                    if(permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected){
                        viewModel.reconnect()
                    }
                }
                if(event == Lifecycle.Event.ON_STOP){
                    if (bleConnectionState == ConnectionState.Connected){
                        viewModel.disconnect()
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    LaunchedEffect(key1 = permissionState.allPermissionsGranted){
        if(permissionState.allPermissionsGranted){
            if(bleConnectionState == ConnectionState.Uninitialized){
                viewModel.initializeConnection()
            }
        }
    }

    if(bleConnectionState == ConnectionState.CurrentlyInitializing){
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            CircularProgressIndicator()
            if(viewModel.initializingMessage != null){
                Text(
                    text = viewModel.initializingMessage!!
                )
            }
        }
    }else if(!permissionState.allPermissionsGranted){
        Text(
            text = "Go to the app setting and allow the missing permissions.",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center
        )
    }else if(viewModel.errorMessage != null){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
           Text(
               text = viewModel.errorMessage!!
           )
            Button(
                onClick = {
                    if(permissionState.allPermissionsGranted){
                        viewModel.initializeConnection()
                    }
                }
            ) {
                Text(
                    "Try again"
                )
            }
        }
    }else if(bleConnectionState == ConnectionState.Connected){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GamePadScreen(viewModel)
        };
    }else if(bleConnectionState == ConnectionState.Disconnected){
        Button(onClick = {
            viewModel.initializeConnection()
        }) {
            Text("Initialize again")
        }
    }
}

@Composable
fun GamePadScreen(
    viewModel: TempHumidityViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Button container
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    ControlButton("▲", onStartClick = { viewModel.sendCommandToBLEDevice("f") }, onStopClick = { viewModel.sendCommandToBLEDevice("s") } )
                }
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    ControlButton("◄", onStartClick = { viewModel.sendCommandToBLEDevice("l") }, onStopClick = { viewModel.sendCommandToBLEDevice("s") } )
                    ControlButton("📷", onStartClick = {  } , onStopClick = {  } )
                    ControlButton("►", onStartClick = { viewModel.sendCommandToBLEDevice("r") }, onStopClick = { viewModel.sendCommandToBLEDevice("s") } )

                }
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    ControlButton("▼", onStartClick = { viewModel.sendCommandToBLEDevice("b") }, onStopClick = { viewModel.sendCommandToBLEDevice("s") } )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ControlButton(text: String, onStartClick: () -> Unit, onStopClick: () -> Unit) {
    Button(
        onClick = { /* Este click se deja vacío porque el manejo se hace con onTouch */ },
        modifier = Modifier
            .size(80.dp)
            .padding(5.dp)
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        onStartClick()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        onStopClick()
                        true
                    }
                    else -> false
                }
            }
    ) {
        Text(text)
    }
}











