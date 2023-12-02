package com.example.bletutorial.presentation.controlScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bletutorial.model.service.WIFIService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WIFIViewModel @Inject constructor(private val wifiService: WIFIService) : ViewModel() {

    init {

    }

    fun goForward() {
        viewModelScope.launch {
            wifiService.goForward()
        }
    }

    fun goBackward() {
        viewModelScope.launch {
            wifiService.goBackward()
        }
    }

    fun goRight() {
        viewModelScope.launch {
            wifiService.goRight()
        }
    }

    fun goLeft() {
        viewModelScope.launch {
            wifiService.goLeft()
        }
    }

    fun stopMovement() {
        viewModelScope.launch {
            wifiService.stopMovement()
        }
    }

    fun takePhoto() {
        viewModelScope.launch {
            wifiService.takePhoto()
        }
    }

    fun takeMeasure() {
        viewModelScope.launch {
            wifiService.takeMeasure()
        }
    }
}
