package com.example.bletutorial.model.repository
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bletutorial.model.data.WIFIResult
import com.example.bletutorial.model.service.WIFIService
import com.example.bletutorial.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class WIFIRepository @Inject constructor(
    private val context: Context
) : WIFIService {
    private val client = OkHttpClient()
    override val data: MutableSharedFlow<Resource<WIFIResult>> = MutableSharedFlow()
    private var webSocket: WebSocket? = null
    private val _imageLiveData = MutableLiveData<Bitmap>()
    override val imageLiveData: LiveData<Bitmap>
        get() = _imageLiveData

    override suspend fun createWebSocketClient() {
        val request = Request.Builder().url("ws://192.168.208.137:81").build()
        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocketOpened: WebSocket, response: Response) {
                Log.d("WebSocket", "WebSocket connection opened")
                webSocket = webSocketOpened
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received message: $text")
                try {
                    // Decode the base64 string to a Bitmap
                    val decodedString = Base64.decode(text, Base64.DEFAULT)
                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                    // Check if the bitmap was successfully decoded and post it to the LiveData
                    decodedBitmap?.let { bitmap ->
                        _imageLiveData.postValue(bitmap)
                    } ?: run {
                        Log.e("WebSocket", "Received string is not a valid base64 encoded image")
                    }
                } catch (e: IllegalArgumentException) {
                    // Handle the case where the text is not a base64 encoded string
                    Log.e("WebSocket", "Received string is not a valid base64", e)
                }
            }


            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closing: $code / $reason")
                // Handle connection closing
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error on WebSocket", t)
                // Handle errors and failures
            }
        })
    }
    private fun processServerResponse(response: String) {
        // Process the response from the server here
        // This could include updating your UI, data models, etc.
    }

    private fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    override suspend  fun goForward() {
        sendMessage("forward")
    }

    override suspend  fun goBackward() {
        sendMessage("backward")
    }

    override suspend fun goRight() {
        sendMessage("right")
    }

    override suspend fun goLeft() {
        sendMessage("left")
    }

    override suspend fun stopMovement() {
        sendMessage("stop")
    }

    override suspend fun takePhoto() {
        val result = sendMessage("photo")
    }

    override  suspend fun takeMeasure() {
        val result = sendMessage("measure")
    }

    private fun base64ToBitmap(base64Str: String): Bitmap? {
        val decodedBytes = Base64.decode(base64Str.substring(base64Str.indexOf(",") + 1), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

}
