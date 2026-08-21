package com.gendemik.chordpusher

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.gendemik.chordpusher.ui.ChordPusherScreen

/**
 * Application entry point.
 *
 * This activity:
 *  - Keeps the screen awake (required for live performance).
 *  - Enables edge-to-edge immersive layout.
 *  - Hosts the single [ChordPusherScreen] composable.
 *
 * A real Ableton Live bridge (USB MIDI or OSC/WebSocket) should be
 * created here and injected into the ControllerViewModel via
 * `vm.setBridge(bridge)` once the MIDI endpoint is discovered.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on during performance.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()

        setContent {
            ChordPusherScreen(
                // ViewModel is provided by the default viewModel() factory inside the composable.
                // To inject a real bridge, obtain the ViewModel here first:
                //   val vm: ControllerViewModel = viewModel()
                //   LaunchedEffect(Unit) { vm.setBridge(UsbMidiBridge(this@MainActivity)) }
                // and pass it into ChordPusherScreen(vm = vm).
            )
        }
    }
}
