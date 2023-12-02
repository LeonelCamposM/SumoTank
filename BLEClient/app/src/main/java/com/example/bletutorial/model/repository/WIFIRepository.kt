
import android.content.Context
import android.util.Log
import com.example.bletutorial.model.data.WIFIResult
import com.example.bletutorial.model.service.WIFIService
import com.example.bletutorial.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class WIFIRepository @Inject constructor(
    private val context: Context
) : WIFIService {
    companion object {
        private const val BASE_URL = "http://192.168.4.1"
    }

    private val client = OkHttpClient()
    override val data: MutableSharedFlow<Resource<WIFIResult>> = MutableSharedFlow()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private suspend fun sendRequest(endpoint: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url("$BASE_URL/$endpoint").build()
        try {
            client.newCall(request).execute().use { response ->
                response.body()?.string() ?: ""
                Log.e( "done ", "${ response.body()?.string() ?: ""}")
            }
        } catch (e: Exception) {
            Log.e( "Error ", "${e.message}")
        }
    } as String

    override suspend  fun goForward() {
        sendRequest("forward")
    }

    override suspend  fun goBackward() {
        sendRequest("backward")
    }

    override suspend fun goRight() {
        sendRequest("right")
    }

    override suspend fun goLeft() {
        sendRequest("left")
    }

    override suspend fun stopMovement() {
        sendRequest("stop")
    }

    override suspend fun takePhoto() {
        val result = sendRequest("photo")
    }

    override  suspend fun takeMeasure() {
        val result = sendRequest("measure")
    }
}
