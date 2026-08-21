package com.gendemik.chordpusher.viewmodel

import androidx.lifecycle.ViewModel
import com.gendemik.chordpusher.midi.BridgeStateListener
import com.gendemik.chordpusher.midi.LiveBridge
import com.gendemik.chordpusher.midi.NoOpBridge
import com.gendemik.chordpusher.model.ConnectionState
import com.gendemik.chordpusher.model.ControllerState
import com.gendemik.chordpusher.model.PadCell
import com.gendemik.chordpusher.model.PadMode
import com.gendemik.chordpusher.model.PadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ControllerViewModel : ViewModel() {

    // The bridge can be swapped at runtime (mock → USB MIDI → WebSocket).
    private var bridge: LiveBridge = NoOpBridge()

    private val _state = MutableStateFlow(ControllerState(pads = mockSessionPads()))
    val state: StateFlow<ControllerState> = _state.asStateFlow()

    private val bridgeListener = object : BridgeStateListener {
        override fun onConnectionChanged(connected: Boolean) {
            _state.update {
                it.copy(
                    connectionState = if (connected) ConnectionState.CONNECTED
                    else ConnectionState.DISCONNECTED
                )
            }
        }

        override fun onTrackChanged(name: String, deviceName: String) {
            _state.update { it.copy(selectedTrackName = name, selectedDeviceName = deviceName) }
        }

        override fun onTransportChanged(playing: Boolean, recording: Boolean, bpm: Float) {
            _state.update { it.copy(isPlaying = playing, isRecording = recording, bpm = bpm) }
        }

        override fun onClipStateChanged(track: Int, scene: Int, state: String) {
            // Update the matching pad cell with the new clip state.
            _state.update { current ->
                val newPads = current.pads.map { pad ->
                    if (pad.col == track && pad.row == scene) {
                        pad.copy(state = clipStateFromString(state))
                    } else {
                        pad
                    }
                }
                current.copy(pads = newPads)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Bridge lifecycle
    // -------------------------------------------------------------------------

    fun setBridge(newBridge: LiveBridge) {
        bridge.setStateListener(null)
        bridge.close()
        bridge = newBridge
        bridge.setStateListener(bridgeListener)
        _state.update { it.copy(connectionState = ConnectionState.CONNECTING) }
    }

    // -------------------------------------------------------------------------
    // Transport
    // -------------------------------------------------------------------------

    fun onPlay() {
        bridge.transportPlay()
        // Optimistic local update; Live will confirm via bridge callback.
        _state.update { it.copy(isPlaying = true) }
    }

    fun onStop() {
        bridge.transportStop()
        _state.update { it.copy(isPlaying = false) }
    }

    fun onRecord() {
        bridge.transportRecord()
        _state.update { it.copy(isRecording = !it.isRecording) }
    }

    fun onUndo() {
        bridge.sendMidi(0xB0, 0x54, 127)  // control-surface undo message placeholder
    }

    fun onPanic() {
        bridge.panic()
    }

    // -------------------------------------------------------------------------
    // Track / channel / octave navigation
    // -------------------------------------------------------------------------

    fun onTrackNext() = bridge.trackNext()
    fun onTrackPrevious() = bridge.trackPrevious()

    fun onChannelNext() {
        _state.update { it.copy(midiChannel = (it.midiChannel % 16) + 1) }
    }

    fun onChannelPrevious() {
        _state.update { it.copy(midiChannel = if (it.midiChannel <= 1) 15 else it.midiChannel - 1) }
    }

    fun onOctaveUp() {
        _state.update { it.copy(octave = (it.octave + 1).coerceAtMost(8)) }
    }

    fun onOctaveDown() {
        _state.update { it.copy(octave = (it.octave - 1).coerceAtLeast(0)) }
    }

    // -------------------------------------------------------------------------
    // Mode selection
    // -------------------------------------------------------------------------

    fun onModeSelected(mode: PadMode) {
        _state.update { current ->
            current.copy(
                padMode = mode,
                pads = when (mode) {
                    PadMode.SESSION -> mockSessionPads()
                    PadMode.NOTE    -> mockNotePads(current.octave)
                    PadMode.DRUM    -> mockDrumPads()
                    PadMode.CHORD   -> mockChordPads(current.key, current.scale)
                    PadMode.SEQ     -> mockSeqPads()
                    PadMode.MIX     -> emptyGrid()
                }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Pad interaction
    // -------------------------------------------------------------------------

    fun onPadPressed(pad: PadCell) {
        when (_state.value.padMode) {
            PadMode.SESSION -> bridge.sendMidi(0x90, pad.midiNote, 127)
            PadMode.NOTE,
            PadMode.DRUM    -> bridge.noteOn(_state.value.midiChannel, pad.midiNote, 100)
            PadMode.CHORD   -> bridge.sendMidi(0x90, pad.midiNote, 127)
            PadMode.SEQ     -> toggleSeqStep(pad)
            PadMode.MIX     -> Unit
        }
    }

    fun onPadReleased(pad: PadCell) {
        when (_state.value.padMode) {
            PadMode.NOTE,
            PadMode.DRUM -> bridge.noteOff(_state.value.midiChannel, pad.midiNote)
            else         -> Unit
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun toggleSeqStep(pad: PadCell) {
        _state.update { current ->
            val newPads = current.pads.map {
                if (it.row == pad.row && it.col == pad.col) {
                    it.copy(
                        state = if (it.state == PadState.STEP_ACTIVE) PadState.EMPTY
                        else PadState.STEP_ACTIVE
                    )
                } else it
            }
            current.copy(pads = newPads)
        }
    }

    private fun clipStateFromString(state: String): PadState = when (state) {
        "playing"   -> PadState.PLAYING
        "recording" -> PadState.RECORDING
        "present"   -> PadState.PRESENT
        else        -> PadState.EMPTY
    }

    override fun onCleared() {
        bridge.setStateListener(null)
        bridge.close()
    }

    // -------------------------------------------------------------------------
    // Mock data generators (replaced by real bridge data at runtime)
    // -------------------------------------------------------------------------

    companion object {

        fun mockSessionPads(): List<PadCell> = buildList {
            val mockStates = arrayOf(
                arrayOf(PadState.PLAYING, PadState.PRESENT, PadState.PRESENT, PadState.EMPTY,
                        PadState.PRESENT, PadState.EMPTY, PadState.EMPTY, PadState.EMPTY),
                arrayOf(PadState.PRESENT, PadState.PRESENT, PadState.EMPTY, PadState.PRESENT,
                        PadState.EMPTY, PadState.EMPTY, PadState.PRESENT, PadState.EMPTY),
                arrayOf(PadState.EMPTY, PadState.PLAYING, PadState.PRESENT, PadState.PRESENT,
                        PadState.PRESENT, PadState.EMPTY, PadState.EMPTY, PadState.EMPTY),
                arrayOf(PadState.PRESENT, PadState.EMPTY, PadState.PRESENT, PadState.EMPTY,
                        PadState.EMPTY, PadState.PRESENT, PadState.EMPTY, PadState.EMPTY),
                arrayOf(PadState.RECORDING, PadState.PRESENT, PadState.EMPTY, PadState.EMPTY,
                        PadState.PRESENT, PadState.PRESENT, PadState.EMPTY, PadState.EMPTY),
                arrayOf(PadState.EMPTY, PadState.EMPTY, PadState.EMPTY, PadState.PRESENT,
                        PadState.EMPTY, PadState.EMPTY, PadState.EMPTY, PadState.EMPTY),
                arrayOf(PadState.EMPTY, PadState.EMPTY, PadState.EMPTY, PadState.EMPTY,
                        PadState.EMPTY, PadState.EMPTY, PadState.EMPTY, PadState.EMPTY),
                arrayOf(PadState.EMPTY, PadState.EMPTY, PadState.EMPTY, PadState.EMPTY,
                        PadState.EMPTY, PadState.EMPTY, PadState.EMPTY, PadState.EMPTY),
            )
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    add(PadCell(row = row, col = col, state = mockStates[row][col],
                        midiNote = row * 8 + col))
                }
            }
        }

        fun mockNotePads(octave: Int): List<PadCell> = buildList {
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    val note = (octave * 12) + col + ((7 - row) * 5)
                    val isRoot = note % 12 == 0
                    val state = if (isRoot) PadState.ROOT_NOTE else PadState.SCALE_NOTE
                    add(PadCell(row = row, col = col, state = state, midiNote = note,
                        label = if (isRoot) "C" else ""))
                }
            }
        }

        fun mockDrumPads(): List<PadCell> {
            val names = listOf(
                "KICK 909", "SNARE", "CLOSED HH", "OPEN HH",
                "CLAP", "TOM LO", "TOM MID", "TOM HI",
                "PERC 1", "PERC 2", "COWBELL", "SHAKER",
                "RIDE", "CRASH", "FX 1", "FX 2",
            )
            return buildList {
                for (row in 0 until 8) {
                    for (col in 0 until 8) {
                        val idx = row * 8 + col
                        val name = if (idx < names.size) names[idx] else ""
                        val state = if (name.isNotEmpty()) PadState.PRESENT else PadState.EMPTY
                        add(PadCell(row = row, col = col, state = state,
                            label = name, midiNote = 36 + idx))
                    }
                }
            }
        }

        fun mockChordPads(key: String, scale: String): List<PadCell> = buildList {
            // Roman numerals for a diatonic chord map — placeholder labels
            val romanNumerals = listOf("I", "ii", "iii", "IV", "V", "vi", "vii°", "I")
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    val chordIdx = (row + col) % 7
                    val state = when (chordIdx) {
                        0    -> PadState.CHORD_STRONG
                        3, 4 -> PadState.CHORD_GOOD
                        6    -> PadState.CHORD_COLOR
                        else -> PadState.PRESENT
                    }
                    add(PadCell(row = row, col = col, state = state,
                        label = romanNumerals[chordIdx],
                        midiNote = 48 + chordIdx * 5))
                }
            }
        }

        fun mockSeqPads(): List<PadCell> = buildList {
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    val stepIdx = row * 8 + col
                    val state = if (stepIdx % 4 == 0) PadState.STEP_ACTIVE else PadState.EMPTY
                    add(PadCell(row = row, col = col, state = state, midiNote = stepIdx))
                }
            }
        }

        fun emptyGrid(): List<PadCell> = buildList {
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    add(PadCell(row = row, col = col))
                }
            }
        }
    }
}
