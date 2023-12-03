package com.example.bletutorial.model.service

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.example.bletutorial.model.data.WIFIResult
import com.example.bletutorial.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface WIFIService {
    val data: MutableSharedFlow<Resource<WIFIResult>>
    val imageLiveData: LiveData<Bitmap>
    suspend fun goForward()
    suspend fun goBackward()
    suspend fun goRight()
    suspend fun goLeft()
    suspend fun stopMovement()
    suspend fun takePhoto()
    suspend fun takeMeasure()
    suspend fun createWebSocketClient()
}