package com.gendemik.chordpusher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gendemik.chordpusher.model.ControllerState
import com.gendemik.chordpusher.ui.theme.ChordPusherColors

@Composable
fun TransportControls(
    state: ControllerState,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onRecord: () -> Unit,
    onUndo: () -> Unit,
    onPanic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChordPusherColors.Surface)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommandButton(
            label = "▶ PLAY",
            active = state.isPlaying,
            accentColor = ChordPusherColors.ButtonPlay,
            modifier = Modifier.weight(1f),
            onClick = onPlay,
        )
        CommandButton(
            label = "■ STOP",
            active = !state.isPlaying,
            accentColor = ChordPusherColors.ButtonStop,
            modifier = Modifier.weight(1f),
            onClick = onStop,
        )
        CommandButton(
            label = "● REC",
            active = state.isRecording,
            accentColor = ChordPusherColors.ButtonRecord,
            modifier = Modifier.weight(1f),
            onClick = onRecord,
        )
        CommandButton(
            label = "UNDO",
            modifier = Modifier.weight(1f),
            onClick = onUndo,
        )
        CommandButton(
            label = "PANIC",
            accentColor = ChordPusherColors.ButtonPanic,
            modifier = Modifier.weight(1f),
            onClick = onPanic,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun TransportControlsPreview() {
    TransportControls(
        state = ControllerState(isPlaying = true),
        onPlay = {}, onStop = {}, onRecord = {}, onUndo = {}, onPanic = {},
    )
}
