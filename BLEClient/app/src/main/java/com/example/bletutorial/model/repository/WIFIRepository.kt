package com.example.bletutorial.model.repository
import android.content.Context
import android.util.Log
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

    override suspend fun createWebSocketClient() {
        val request = Request.Builder().url("ws://192.168.208.137:81").build()
        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocketOpened: WebSocket, response: Response) {
                Log.d("WebSocket", "WebSocket connection opened")
                webSocket = webSocketOpened
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received message: $text")
                // Handle incoming messages
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
}
