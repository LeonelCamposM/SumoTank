package com.example.bletutorial.model.repository

import android.annotation.SuppressLint
import android.content.Context
import android.view.InputDevice
import android.view.MotionEvent
import com.example.bletutorial.model.data.JoystickResult
import com.example.bletutorial.model.domain.JoystickState
import com.example.bletutorial.model.service.JoystickService
import com.example.bletutorial.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@SuppressLint("MissingPermission")
class JoystickRepository @Inject constructor(
    private val context: Context
) : JoystickService {
    override val data: MutableSharedFlow<Resource<JoystickResult>> = MutableSharedFlow()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        emitResult(ResourceStatus.SUCCESS,  JoystickResult(JoystickState.Center))
    }

    enum class ResourceStatus {
        SUCCESS, ERROR, LOADING
    }

    private fun emitResult(
        status: ResourceStatus,
        data: JoystickResult? = null,
        message: String? = null
    ) {
        coroutineScope.launch {
            val resource = when (status) {
                ResourceStatus.SUCCESS -> Resource.Success(data ?: JoystickResult(JoystickState.Center))
                ResourceStatus.ERROR -> Resource.Error(message ?: "Unknown error")
                ResourceStatus.LOADING -> Resource.Loading(data, message)
            }
            this@JoystickRepository.data.emit(resource)
        }
    }

    override fun processJoystickInput(event: MotionEvent, historyPos: Int) {
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
    private fun updateJoystickState(x: Float, y: Float) {
        val newState = when {
            x == 0f && y == 0f -> JoystickState.Center
            x < 0 -> JoystickState.Left
            x > 0 -> JoystickState.Right
            y < 0 -> JoystickState.Forward
            y > 0 -> JoystickState.Backward
            else -> JoystickState.Center
        }
        emitResult(ResourceStatus.SUCCESS,  JoystickResult(newState))
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