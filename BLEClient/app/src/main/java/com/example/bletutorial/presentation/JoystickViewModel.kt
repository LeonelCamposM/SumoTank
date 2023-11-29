package com.example.bletutorial.presentation

import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class JoystickViewModel : ViewModel() {
    private val _joystickState = MutableStateFlow<JoystickState>(JoystickState.Initial)
    val joystickState: StateFlow<JoystickState> = _joystickState.asStateFlow()
    private fun updateJoystickState(x: Float, y: Float) {
        val state = when {
            x == 0f && y == 0f -> JoystickState.Center
            x < 0 -> JoystickState.Left
            x > 0 -> JoystickState.Right
            y < 0 -> JoystickState.Forward
            y > 0 -> JoystickState.Backward
            else -> JoystickState.Center
        }
        Log.e("Direction:", state.toString())
        _joystickState.value = state
    }

    fun processJoystickInput(
        event: MotionEvent,
        historyPos: Int
    ) {
        val mInputDevice = event.device
        val lx = getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_X, historyPos
        )
        val rx = getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_Z, historyPos
        )
        val ly = getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_Y, historyPos
        )
        val ry = getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_RZ, historyPos
        )
        val lxFloat = lx as? Float ?: 0f
        val lyFloat = ly as? Float ?: 0f
        updateJoystickState(lxFloat, lyFloat)
    }

    private fun getCenteredAxis(
        event: MotionEvent,
        device: InputDevice, axis: Int, historyPos: Int
    ): Float  {
        val range = device.getMotionRange(axis, event.source)
        if (range != null) {
            val flat = range.flat
            val value =
                if (historyPos < 0) event.getAxisValue(axis) else event.getHistoricalAxisValue(
                    axis,
                    historyPos
                )
            if (abs(value) > flat) {
                return value
            }
        }
        return 0f
    }
}


sealed class JoystickState {
    object Initial : JoystickState()
    object Center : JoystickState()
    object Left : JoystickState()
    object Right : JoystickState()
    object Forward : JoystickState()
    object Backward : JoystickState()
}