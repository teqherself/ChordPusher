package com.gendemik.chordpusher.midi

/**
 * Thin abstraction for the MIDI/network bridge.
 *
 * Implementations can target:
 *  - Android MIDI API (USB)
 *  - OSC over UDP
 *  - WebSocket bridge
 *
 * For the v1.0 starter this interface is intentionally simple;
 * real implementations will extend it as the protocol is finalised.
 */
interface LiveBridge {

    /** True when a two-way connection to the Live Remote Script is active. */
    val isConnected: Boolean

    /** Send a raw MIDI message (status, data1, data2). */
    fun sendMidi(status: Int, data1: Int, data2: Int)

    /** Send a NOTE ON on the given channel (1–15). */
    fun noteOn(channel: Int, note: Int, velocity: Int) {
        sendMidi(0x90 or (channel - 1), note, velocity)
    }

    /** Send a NOTE OFF on the given channel (1–15). */
    fun noteOff(channel: Int, note: Int) {
        sendMidi(0x80 or (channel - 1), note, 0)
    }

    /** Send all-notes-off on all channels 1–16 (PANIC). */
    fun panic() {
        for (ch in 1..16) {
            sendMidi(0xB0 or (ch - 1), 123, 0)
        }
    }

    /** Start Ableton transport. */
    fun transportPlay()

    /** Stop Ableton transport. */
    fun transportStop()

    /** Toggle record. */
    fun transportRecord()

    /** Select the next track. */
    fun trackNext()

    /** Select the previous track. */
    fun trackPrevious()

    /** Register a listener for state updates pushed from Live. */
    fun setStateListener(listener: BridgeStateListener?)

    /** Release resources. */
    fun close()
}

/**
 * Callback interface for state arriving from the Live Remote Script.
 * All callbacks are delivered on the calling thread; callers must
 * marshal to the main thread themselves if required.
 */
interface BridgeStateListener {
    fun onConnectionChanged(connected: Boolean) {}
    fun onTrackChanged(name: String, deviceName: String) {}
    fun onTransportChanged(playing: Boolean, recording: Boolean, bpm: Float) {}
    fun onClipStateChanged(track: Int, scene: Int, state: String) {}
    fun onPadFeedback(note: Int, velocity: Int) {}
}

/** No-op bridge used for standalone/mock mode. */
class NoOpBridge : LiveBridge {
    override val isConnected: Boolean = false
    override fun sendMidi(status: Int, data1: Int, data2: Int) = Unit
    override fun transportPlay() = Unit
    override fun transportStop() = Unit
    override fun transportRecord() = Unit
    override fun trackNext() = Unit
    override fun trackPrevious() = Unit
    override fun setStateListener(listener: BridgeStateListener?) = Unit
    override fun close() = Unit
}
