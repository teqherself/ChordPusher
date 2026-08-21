package com.gendemik.chordpusher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gendemik.chordpusher.model.PadMode
import com.gendemik.chordpusher.ui.theme.ChordPusherColors

private val modes = listOf(
    PadMode.SESSION to "SESSION",
    PadMode.NOTE    to "NOTE",
    PadMode.DRUM    to "DRUM",
    PadMode.CHORD   to "CHORD",
    PadMode.SEQ     to "SEQ",
    PadMode.MIX     to "MIX",
)

@Composable
fun ModeSelectorRow(
    selectedMode: PadMode,
    onModeSelected: (PadMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChordPusherColors.Surface)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        modes.forEach { (mode, label) ->
            CommandButton(
                label = label,
                active = selectedMode == mode,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(mode) },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun ModeSelectorRowPreview() {
    ModeSelectorRow(selectedMode = PadMode.SESSION, onModeSelected = {})
}
