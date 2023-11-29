package com.example.bletutorial.model.service

import android.view.MotionEvent
import com.example.bletutorial.model.data.JoystickResult
import com.example.bletutorial.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface JoystickService {
    val data: MutableSharedFlow<Resource<JoystickResult>>
    fun processJoystickInput(event: MotionEvent,
                             historyPos: Int)
}
