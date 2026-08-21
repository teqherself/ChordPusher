package com.gendemik.chordpusher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gendemik.chordpusher.model.ControllerState
import com.gendemik.chordpusher.ui.theme.ChordPusherColors

@Composable
fun TrackChannelControls(
    state: ControllerState,
    onTrackPrevious: () -> Unit,
    onTrackNext: () -> Unit,
    onChannelPrevious: () -> Unit,
    onChannelNext: () -> Unit,
    onOctaveDown: () -> Unit,
    onOctaveUp: () -> Unit,
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
        CommandButton(label = "TRK−", modifier = Modifier.weight(1f), onClick = onTrackPrevious)
        CommandButton(label = "TRK+", modifier = Modifier.weight(1f), onClick = onTrackNext)

        Text(
            text = "|",
            color = ChordPusherColors.Divider,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
        )

        CommandButton(label = "CH−", modifier = Modifier.weight(1f), onClick = onChannelPrevious)
        CommandButton(label = "CH+", modifier = Modifier.weight(1f), onClick = onChannelNext)

        Text(
            text = "|",
            color = ChordPusherColors.Divider,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
        )

        CommandButton(label = "OCT−", modifier = Modifier.weight(1f), onClick = onOctaveDown)
        Text(
            text = "${state.octave}",
            color = ChordPusherColors.TextEmphasis,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        CommandButton(label = "OCT+", modifier = Modifier.weight(1f), onClick = onOctaveUp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun TrackChannelControlsPreview() {
    TrackChannelControls(
        state = ControllerState(octave = 4),
        onTrackPrevious = {}, onTrackNext = {},
        onChannelPrevious = {}, onChannelNext = {},
        onOctaveDown = {}, onOctaveUp = {},
    )
}
