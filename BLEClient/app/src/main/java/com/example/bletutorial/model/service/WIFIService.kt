package com.example.bletutorial.model.service

import com.example.bletutorial.model.data.WIFIResult
import com.example.bletutorial.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface WIFIService {
    val data: MutableSharedFlow<Resource<WIFIResult>>
    suspend fun goForward()
    suspend fun goBackward()
    suspend fun goRight()
    suspend fun goLeft()
    suspend fun stopMovement()
    suspend fun takePhoto()
    suspend fun takeMeasure()
    suspend fun createWebSocketClient()
}