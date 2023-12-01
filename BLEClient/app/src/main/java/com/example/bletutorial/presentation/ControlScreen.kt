package com.example.bletutorial.presentation

import android.bluetooth.BluetoothAdapter
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.example.bletutorial.model.domain.JoystickState
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ControlScreen(
    onBluetoothStateChanged:()->Unit,
    bleServiceViewModel: BLEServiceViewModel = hiltViewModel(),
    joystickViewModel: JoystickViewModel = hiltViewModel()
)
{
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

    LaunchedEffect(joystickState, bleConnectionState) {
        if (bleServiceViewModel.isLaunchedEffectActive) {
            if (bleConnectionState == ConnectionState.Connected) {
                when (joystickState) {
                    JoystickState.Forward -> bleServiceViewModel.sendCommandToBLEDevice("f")
                    JoystickState.Backward -> bleServiceViewModel.sendCommandToBLEDevice("b")
                    JoystickState.Left -> bleServiceViewModel.sendCommandToBLEDevice("l")
                    JoystickState.Right -> bleServiceViewModel.sendCommandToBLEDevice("r")
                    JoystickState.Center -> bleServiceViewModel.sendCommandToBLEDevice("s")
                }
            }
        }
    }


    LaunchedEffect(joystickState, bleConnectionState, bleServiceViewModel.isLaunchedEffectActive) {
        while (true) {
            if (bleServiceViewModel.isLaunchedEffectActive) {
                if (bleConnectionState == ConnectionState.Connected &&
                    joystickState == JoystickState.Center) {
                    bleServiceViewModel.sendCommandToBLEDevice("s")
                }
            }
            delay(1000/6) // Wait for 1/6 second before checking the state again
        }
    }

    when (bleConnectionState) {
        ConnectionState.Uninitialized -> InitializingUI(bleServiceViewModel)
        ConnectionState.CurrentlyInitializing -> InitializingUI(bleServiceViewModel)
        ConnectionState.Connected ->  ConnectedUI(bleServiceViewModel, joystickViewModel)
        ConnectionState.Disconnected-> DisconnectedUI(bleServiceViewModel)
    }

    if(!permissionState.allPermissionsGranted){
        Text(
            text = "Go to the app setting and allow the missing permissions.",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center,
            color = Color.White
        )
    }else if(bleServiceViewModel.errorMessage != null){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
           Text(
               text = bleServiceViewModel.errorMessage!!,
               color = Color.White
           )
            Button(
                onClick = {
                    if(permissionState.allPermissionsGranted){
                        bleServiceViewModel.initializeConnection()
                    }
                }
            ) {
                Text(
                    "Try again",color = Color.White
                )
            }
        }
    }
}

@Composable
fun InitializingUI(viewModel: BLEServiceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        CircularProgressIndicator()
        if(viewModel.initializingMessage != null){
            Text(
                text = viewModel.initializingMessage!!,
                color = Color.White
            )
        }
    }
}
@Composable
fun SwitchWithLabel(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    labelText: String
) {
    val colors = MaterialTheme.colors
    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(colors.primary, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.primaryVariant, // Color del pulgar cuando está activado
                    uncheckedThumbColor = Color.DarkGray, // Color del pulgar cuando está desactivado (Gris)
                    checkedTrackColor = colors.primaryVariant.copy(alpha = 0.5f), // Color de la pista cuando está activado
                    uncheckedTrackColor = Color.LightGray // Color de la pista cuando está desactivado (Gris claro)
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = labelText
            )
        }
    }
}

@Composable
fun DisconnectedUI(viewModel: BLEServiceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Button(onClick = {
            viewModel.initializeConnection()
        }) {
            Text("Reconnection")
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ControlButton(text: String, onStartClick: () -> Unit, onStopClick: () -> Unit) {
    Button(
        onClick = { },
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
                    ControlButton(
                        "▲",
                        onStartClick = { viewModel.sendCommandToBLEDevice("f") },
                        onStopClick = { viewModel.sendCommandToBLEDevice("s") })
                }
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    ControlButton(
                        "◄",
                        onStartClick = { viewModel.sendCommandToBLEDevice("l") },
                        onStopClick = { viewModel.sendCommandToBLEDevice("s") })
                    ControlButton("📷", onStartClick = { }, onStopClick = { })
                    ControlButton(
                        "►",
                        onStartClick = { viewModel.sendCommandToBLEDevice("r") },
                        onStopClick = { viewModel.sendCommandToBLEDevice("s") })

                }
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    ControlButton(
                        "▼",
                        onStartClick = { viewModel.sendCommandToBLEDevice("b") },
                        onStopClick = { viewModel.sendCommandToBLEDevice("s") })
                }
            }
        }
    }
}

@Composable
fun ConnectedUI(bleServiceViewModel: BLEServiceViewModel, joystickViewModel: JoystickViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        SwitchWithLabel(
            isChecked = bleServiceViewModel.isLaunchedEffectActive,
            onCheckedChange = { isChecked ->
                if (isChecked) {
                    bleServiceViewModel.activateLaunchedEffect()
                } else {
                    bleServiceViewModel.deactivateLaunchedEffect()
                }
            },
            labelText = "Control externo"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (joystickViewModel.joystickState) {
                JoystickState.Forward -> "forward"
                JoystickState.Backward -> "backward"
                JoystickState.Left -> "left"
                JoystickState.Right -> "right"
                JoystickState.Center -> "stop"
            },
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        GamePadScreen(bleServiceViewModel)
    }
}












