package com.example.bletutorial

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bletutorial.presentation.Navigation
import com.example.bletutorial.presentation.theme.BLETutorialTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BLETutorialTheme {
                Navigation(
                    onBluetoothStateChanged = {
                        showBluetoothDialog()
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showBluetoothDialog()
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {

        // Check that the event came from a game controller
        if (event.source and InputDevice.SOURCE_JOYSTICK ==
            InputDevice.SOURCE_JOYSTICK &&
            event.action == MotionEvent.ACTION_MOVE
        ) {

            // Process all historical movement samples in the batch
            val historySize = event.historySize

            // Process the movements starting from the
            // earliest historical position in the batch
            for (i in 0 until historySize) {
                // Process the event at historical position i
                processJoystickInput(event, i)
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(event, -1)
            return true
        }
        return super.onGenericMotionEvent(event)
    }


    private fun processJoystickInput(
        event: MotionEvent,
        historyPos: Int
    ) {
        val mInputDevice = event.device

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
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

        // Determine direction based on LX and LY values
        val direction = when {
            lxFloat < 0 -> "Left"
            lxFloat > 0 -> "Right"
            lyFloat < 0 -> "Forward"
            lyFloat > 0 -> "Backward"
            else -> "Center"
        }

        // Log the direction
        Log.e("Direction:", direction)
    }

    private fun getCenteredAxis(
        event: MotionEvent,
        device: InputDevice, axis: Int, historyPos: Int
    ): Any {
        val range = device.getMotionRange(axis, event.source)

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            val flat = range.flat
            val value =
                if (historyPos < 0) event.getAxisValue(axis) else event.getHistoricalAxisValue(
                    axis,
                    historyPos
                )

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (abs(value) > flat) {
                return value
            }
        }
        return 0
    }

    private var isBluetoothDialogAlreadyShown = false
    private fun showBluetoothDialog(){
        if(!bluetoothAdapter.isEnabled){
            if(!isBluetoothDialogAlreadyShown){
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startBluetoothIntentForResult.launch(enableBluetoothIntent)
                isBluetoothDialogAlreadyShown = true
            }
        }
    }

    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            isBluetoothDialogAlreadyShown = false
            if(result.resultCode != Activity.RESULT_OK){
                showBluetoothDialog()
            }
        }

}
