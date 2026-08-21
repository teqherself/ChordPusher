package com.gendemik.chordpusher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gendemik.chordpusher.model.ConnectionState
import com.gendemik.chordpusher.model.ControllerState
import com.gendemik.chordpusher.ui.theme.ChordPusherColors

@Composable
fun ConnectionStatusBar(state: ControllerState, modifier: Modifier = Modifier) {
    val (statusColor, statusLabel) = when (state.connectionState) {
        ConnectionState.CONNECTED    -> ChordPusherColors.StatusConnected    to "LIVE ●"
        ConnectionState.CONNECTING   -> ChordPusherColors.StatusConnecting   to "CONNECTING…"
        ConnectionState.MIDI_ACTIVE  -> ChordPusherColors.MidiActive         to "MIDI ↔ OK"
        ConnectionState.DISCONNECTED -> ChordPusherColors.StatusDisconnected to "OFFLINE"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChordPusherColors.Surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // App name
        Column {
            Text(
                text = "CHORDPUSHER",
                color = ChordPusherColors.TextEmphasis,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
            )
            Text(
                text = "by Gendemik Digital",
                color = ChordPusherColors.TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Connection status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor, CircleShape)
            )
            Text(
                text = statusLabel,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Track / channel info
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "CH ${state.midiChannel.toString().padStart(2, '0')}",
                color = ChordPusherColors.TextEmphasis,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = state.selectedTrackName.take(12),
                color = ChordPusherColors.TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun ConnectionStatusBarPreview() {
    ConnectionStatusBar(
        state = ControllerState(
            connectionState = ConnectionState.CONNECTED,
            selectedTrackName = "DRUMS",
            midiChannel = 3,
        )
    )
}
