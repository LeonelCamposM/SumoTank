package com.example.bletutorial.presentation

import android.bluetooth.BluetoothAdapter
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
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
import com.example.bletutorial.model.domain.ConnectionState
import com.example.bletutorial.presentation.permissions.PermissionUtils
import com.example.bletutorial.presentation.permissions.SystemBroadcastReceiver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.example.bletutorial.model.domain.JoystickState
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ControlScreen(
    onBluetoothStateChanged:()->Unit,
    bleServiceViewModel: BLEServiceViewModel = hiltViewModel(),
    joystickViewModel: JoystickViewModel = hiltViewModel()
) {
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED){ bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if(action == BluetoothAdapter.ACTION_STATE_CHANGED){
            onBluetoothStateChanged()
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState = lifecycleOwner.lifecycle.currentState
    val bleConnectionState = bleServiceViewModel.connectionState
    val joystickState = joystickViewModel.joystickState

    fun handleStartEvent(
        bleConnectionState: ConnectionState,
        viewModel: BLEServiceViewModel,
        permissionState: MultiplePermissionsState
    ) {
        if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected) {
            viewModel.reconnect()
        }
    }

    fun handleStopEvent(
        bleConnectionState: ConnectionState,
        viewModel: BLEServiceViewModel
    ) {
        if (bleConnectionState == ConnectionState.Connected) {
            viewModel.disconnect()
        }
    }

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.STARTED -> {
                permissionState.launchMultiplePermissionRequest()
                handleStartEvent(bleConnectionState, bleServiceViewModel, permissionState)
            }
            else -> Unit
        }
    }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Uninitialized) {
            bleServiceViewModel.initializeConnection()
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            handleStopEvent(bleConnectionState, bleServiceViewModel)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (joystickState) {
            JoystickState.Forward -> Text("f")
            JoystickState.Backward -> Text("b")
            JoystickState.Left -> Text("l")
            JoystickState.Right -> Text("r")
            JoystickState.Center -> Text("c")
            else -> Unit
        }
    }

    when (bleConnectionState) {
        ConnectionState.CurrentlyInitializing -> InitializingUI(bleServiceViewModel)
        ConnectionState.Disconnected -> DisconnectedUI(bleServiceViewModel)
        ConnectionState.Connected -> ConnectedUI(bleServiceViewModel)
        else -> Unit
    }

    if(!permissionState.allPermissionsGranted){
        Text(
            text = "Go to the app setting and allow the missing permissions.",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center
        )
    }else if(bleServiceViewModel.errorMessage != null){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
           Text(
               text = bleServiceViewModel.errorMessage!!
           )
            Button(
                onClick = {
                    if(permissionState.allPermissionsGranted){
                        bleServiceViewModel.initializeConnection()
                    }
                }
            ) {
                Text(
                    "Try again"
                )
            }
        }
    }
}


@Composable
fun InitializingUI(viewModel: BLEServiceViewModel) {
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
}

@Composable
fun DisconnectedUI(viewModel: BLEServiceViewModel) {
    Button(onClick = {
        viewModel.initializeConnection()
    }) {
        Text("Reconectar")
    }
}

@Composable
fun ConnectedUI(viewModel: BLEServiceViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GamePadScreen(viewModel)
    }
}

@Composable
fun GamePadScreen(
    viewModel: BLEServiceViewModel
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
        onClick = {  },
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











