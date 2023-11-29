package com.example.bletutorial.model.domain

sealed interface JoystickState {
    object Center : JoystickState
    object Left : JoystickState
    object Right : JoystickState
    object Forward : JoystickState
    object Backward : JoystickState
}