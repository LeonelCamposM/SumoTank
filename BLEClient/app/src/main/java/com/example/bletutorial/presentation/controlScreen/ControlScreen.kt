package com.example.bletutorial.presentation.controlScreen

import android.bluetooth.BluetoothAdapter
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bletutorial.model.domain.JoystickState
import com.example.bletutorial.presentation.permissions.SystemBroadcastReceiver

@Composable
fun ControlScreen(
    onBluetoothStateChanged:()->Unit,
    joystickViewModel: JoystickViewModel = hiltViewModel(),
    wifiViewModel: WIFIViewModel = hiltViewModel()
)
{
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED){ bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if(action == BluetoothAdapter.ACTION_STATE_CHANGED){
            onBluetoothStateChanged()
        }
    }
    val joystickState = joystickViewModel.joystickState

    LaunchedEffect(joystickState) {
        when (joystickState) {
            JoystickState.Forward -> wifiViewModel.goForward()
            JoystickState.Backward -> wifiViewModel.goBackward()
            JoystickState.Left -> wifiViewModel.goLeft()
            JoystickState.Right -> wifiViewModel.goRight()
            JoystickState.Center -> wifiViewModel.stopMovement()
        }
    }
    ConnectedUI(wifiViewModel, joystickViewModel)
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
    viewModel: WIFIViewModel
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
                        onStartClick = { viewModel.goForward() },
                        onStopClick = { viewModel.stopMovement() })
                }
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    ControlButton(
                        "◄",
                        onStartClick = { viewModel.goLeft() },
                        onStopClick = { viewModel.stopMovement() })
                    ControlButton("📷", onStartClick = {viewModel.takePhoto() }, onStopClick = { })
                    ControlButton(
                        "►",
                        onStartClick = { viewModel.goRight() },
                        onStopClick = { viewModel.stopMovement() })
                }
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    ControlButton(
                        "▼",
                        onStartClick = { viewModel.goBackward() },
                        onStopClick = { viewModel.stopMovement() })
                }
            }
        }
    }
}

@Composable
fun ConnectedUI(wifiViewModel: WIFIViewModel, joystickViewModel: JoystickViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
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
        GamePadScreen(wifiViewModel)
    }
}











