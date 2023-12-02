package com.example.bletutorial.presentation.controlScreen

import android.util.Log
import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bletutorial.model.domain.JoystickState
import com.example.bletutorial.model.service.JoystickService
import com.example.bletutorial.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoystickViewModel  @Inject constructor (private val joystickService: JoystickService) : ViewModel() {
    var joystickState by mutableStateOf<JoystickState>(JoystickState.Center)
    init {
        subscribeToChanges()
    }

    private fun subscribeToChanges(){
        viewModelScope.launch {
            joystickService.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        Log.e("change", result.data?.joystickState.toString())
                        joystickState = result.data?.joystickState!!
                    }

                    is Resource.Error -> {
                        joystickState = JoystickState.Center
                    }
                }
            }
        }
    }

    fun processJoystickInput(event: MotionEvent,
                             historyPos: Int){
        joystickService.processJoystickInput(event,historyPos)
        Log.e("processes","processes")
    }
}


