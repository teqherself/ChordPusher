package com.gendemik.chordpusher.model

/** The current mode of the 8×8 pad surface. */
enum class PadMode {
    SESSION, NOTE, DRUM, CHORD, SEQ, MIX
}

/** Visual/semantic state of a single pad cell. */
enum class PadState {
    EMPTY,          // no clip / no note in this position
    PRESENT,        // clip present / note available
    PLAYING,        // currently playing
    RECORDING,      // recording
    HELD,           // locally held / latched
    STEP_ACTIVE,    // sequencer step is set
    STEP_PLAYHEAD,  // sequencer playhead position
    ROOT_NOTE,      // scale root landmark
    SCALE_NOTE,     // in-scale note
    CHORD_STRONG,   // strong harmonic recommendation
    CHORD_GOOD,     // good harmonic alternative
    CHORD_COLOR,    // colour / tension move
}

/** Represents a single pad in the 8×8 grid. */
data class PadCell(
    val row: Int,           // 0–7, top to bottom
    val col: Int,           // 0–7, left to right
    val state: PadState = PadState.EMPTY,
    val label: String = "",
    val midiNote: Int = -1,
)

/** Live connection / MIDI status reported by the bridge. */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    MIDI_ACTIVE,
}

/** Snapshot of the app state as surfaced to the UI. */
data class ControllerState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val bpm: Float = 120f,
    val isPlaying: Boolean = false,
    val isRecording: Boolean = false,
    val selectedTrackName: String = "—",
    val selectedDeviceName: String = "—",
    val midiChannel: Int = 1,       // 1–16
    val padMode: PadMode = PadMode.SESSION,
    val pads: List<PadCell> = emptyList(),
    val trackOffset: Int = 0,       // session bank column offset
    val sceneOffset: Int = 0,       // session bank row offset
    val octave: Int = 4,
    val key: String = "C",
    val scale: String = "Major",
)
