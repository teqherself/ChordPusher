package com.gendemik.chordpusher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gendemik.chordpusher.model.PadCell
import com.gendemik.chordpusher.model.PadState
import com.gendemik.chordpusher.ui.theme.ChordPusherColors
import com.gendemik.chordpusher.viewmodel.ControllerViewModel

/** Returns the background colour for a pad based on its state. */
private fun padColor(state: PadState): Color = when (state) {
    PadState.EMPTY         -> ChordPusherColors.PadEmpty
    PadState.PRESENT       -> ChordPusherColors.PadPresent
    PadState.PLAYING       -> ChordPusherColors.PadPlaying
    PadState.RECORDING     -> ChordPusherColors.PadRecording
    PadState.HELD          -> ChordPusherColors.PadHeld
    PadState.STEP_ACTIVE   -> ChordPusherColors.PadStepActive
    PadState.STEP_PLAYHEAD -> ChordPusherColors.PadStepHead
    PadState.ROOT_NOTE     -> ChordPusherColors.PadRoot
    PadState.SCALE_NOTE    -> ChordPusherColors.PadScale
    PadState.CHORD_STRONG  -> ChordPusherColors.PadChordStrong
    PadState.CHORD_GOOD    -> ChordPusherColors.PadChordGood
    PadState.CHORD_COLOR   -> ChordPusherColors.PadChordColor
}

/**
 * The 8×8 pad surface.
 *
 * Callers supply [pads] as a flat list of 64 [PadCell]s ordered
 * row-major (row 0 = top).
 */
@Composable
fun PadSurface(
    pads: List<PadCell>,
    onPadPressed: (PadCell) -> Unit,
    onPadReleased: (PadCell) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ChordPusherColors.Background)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        for (row in 0 until 8) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                for (col in 0 until 8) {
                    val pad = pads.getOrElse(row * 8 + col) {
                        PadCell(row = row, col = col)
                    }
                    PadCellView(
                        pad = pad,
                        onPressed = { onPadPressed(pad) },
                        onReleased = { onPadReleased(pad) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PadCellView(
    pad: PadCell,
    onPressed: () -> Unit,
    onReleased: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = padColor(pad.state)
    val borderColor = bgColor.copy(alpha = 0.6f)
    val shape = RoundedCornerShape(3.dp)

    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .pointerInput(pad) {
                detectTapGestures(
                    onPress = {
                        onPressed()
                        tryAwaitRelease()
                        onReleased()
                    }
                )
            },
    ) {
        if (pad.label.isNotEmpty()) {
            Text(
                text = pad.label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun PadSurfacePreview() {
    PadSurface(
        pads = ControllerViewModel.mockSessionPads(),
        onPadPressed = {},
        onPadReleased = {},
    )
}
