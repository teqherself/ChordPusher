package com.gendemik.chordpusher.ui.theme

import androidx.compose.ui.graphics.Color

// Gendemik Digital brand palette
object ChordPusherColors {
    val Background     = Color(0xFF0D0D0D)
    val Surface        = Color(0xFF1A1A1A)
    val SurfaceVariant = Color(0xFF252525)
    val Divider        = Color(0xFF333333)

    // Pad semantic colours (match the spec)
    val PadPlaying      = Color(0xFF00E676)   // green — playing
    val PadRecording    = Color(0xFFFF1744)   // red — recording
    val PadPresent      = Color(0xFF1565C0)   // blue dim — clip present
    val PadHeld         = Color(0xFFFFD600)   // gold — locally held
    val PadRoot         = Color(0xFFE040FB)   // purple — root note
    val PadScale        = Color(0xFF26C6DA)   // cyan — in-scale
    val PadChordStrong  = Color(0xFF00E676)   // green — strong harmonic
    val PadChordGood    = Color(0xFF2979FF)   // blue — good alternative
    val PadChordColor   = Color(0xFFAA00FF)   // purple — colour/tension
    val PadStepActive   = Color(0xFFFFAB00)   // amber — step on
    val PadStepHead     = Color(0xFF00B0FF)   // cyan — playhead
    val PadEmpty        = Color(0xFF1F1F1F)   // near-black — empty

    // Transport / status colours
    val StatusConnected    = Color(0xFF00E676)
    val StatusConnecting   = Color(0xFFFFAB00)
    val StatusDisconnected = Color(0xFFB71C1C)
    val MidiActive         = Color(0xFF00B0FF)

    // Control button colours
    val ButtonPlay    = Color(0xFF00C853)
    val ButtonStop    = Color(0xFFE53935)
    val ButtonRecord  = Color(0xFFFF1744)
    val ButtonPanic   = Color(0xFFD50000)
    val ButtonNeutral = Color(0xFF37474F)
    val ButtonActive  = Color(0xFFFFD600)

    // Mode selector colours
    val ModeActive   = Color(0xFFFFD600)
    val ModeInactive = Color(0xFF37474F)

    // Text
    val TextPrimary   = Color(0xFFEEEEEE)
    val TextSecondary = Color(0xFF757575)
    val TextEmphasis  = Color(0xFFFFD600)
}
