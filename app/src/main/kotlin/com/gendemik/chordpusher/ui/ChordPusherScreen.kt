package com.gendemik.chordpusher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gendemik.chordpusher.model.ControllerState
import com.gendemik.chordpusher.ui.components.ConnectionStatusBar
import com.gendemik.chordpusher.ui.components.ModeSelectorRow
import com.gendemik.chordpusher.ui.components.PadSurface
import com.gendemik.chordpusher.ui.components.TrackChannelControls
import com.gendemik.chordpusher.ui.components.TransportControls
import com.gendemik.chordpusher.ui.theme.ChordPusherColors
import com.gendemik.chordpusher.viewmodel.ControllerViewModel

/**
 * Root screen for ChordPusher.
 *
 * Layout (portrait, top → bottom):
 *  1. Connection / status bar
 *  2. Mode selector row (SESSION | NOTE | DRUM | CHORD | SEQ | MIX)
 *  3. Track / channel / octave navigation
 *  4. Transport controls (PLAY | STOP | REC | UNDO | PANIC)
 *  5. 8×8 performance pad surface (fills remaining vertical space)
 */
@Composable
fun ChordPusherScreen(vm: ControllerViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    ChordPusherScreenContent(
        state = state,
        onModeSelected = vm::onModeSelected,
        onTrackPrevious = vm::onTrackPrevious,
        onTrackNext = vm::onTrackNext,
        onChannelPrevious = vm::onChannelPrevious,
        onChannelNext = vm::onChannelNext,
        onOctaveDown = vm::onOctaveDown,
        onOctaveUp = vm::onOctaveUp,
        onPlay = vm::onPlay,
        onStop = vm::onStop,
        onRecord = vm::onRecord,
        onUndo = vm::onUndo,
        onPanic = vm::onPanic,
        onPadPressed = vm::onPadPressed,
        onPadReleased = vm::onPadReleased,
    )
}

/** Stateless content composable (testable without ViewModel). */
@Composable
fun ChordPusherScreenContent(
    state: ControllerState,
    onModeSelected: (com.gendemik.chordpusher.model.PadMode) -> Unit,
    onTrackPrevious: () -> Unit,
    onTrackNext: () -> Unit,
    onChannelPrevious: () -> Unit,
    onChannelNext: () -> Unit,
    onOctaveDown: () -> Unit,
    onOctaveUp: () -> Unit,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onRecord: () -> Unit,
    onUndo: () -> Unit,
    onPanic: () -> Unit,
    onPadPressed: (com.gendemik.chordpusher.model.PadCell) -> Unit,
    onPadReleased: (com.gendemik.chordpusher.model.PadCell) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChordPusherColors.Background),
    ) {
        ConnectionStatusBar(state = state)

        ModeSelectorRow(
            selectedMode = state.padMode,
            onModeSelected = onModeSelected,
        )

        TrackChannelControls(
            state = state,
            onTrackPrevious = onTrackPrevious,
            onTrackNext = onTrackNext,
            onChannelPrevious = onChannelPrevious,
            onChannelNext = onChannelNext,
            onOctaveDown = onOctaveDown,
            onOctaveUp = onOctaveUp,
        )

        TransportControls(
            state = state,
            onPlay = onPlay,
            onStop = onStop,
            onRecord = onRecord,
            onUndo = onUndo,
            onPanic = onPanic,
        )

        PadSurface(
            pads = state.pads,
            onPadPressed = onPadPressed,
            onPadReleased = onPadReleased,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF0D0D0D,
    widthDp = 400,
    heightDp = 820,
)
@Composable
private fun ChordPusherScreenPreview() {
    ChordPusherScreenContent(
        state = ControllerState(
            pads = ControllerViewModel.mockSessionPads(),
        ),
        onModeSelected = {},
        onTrackPrevious = {}, onTrackNext = {},
        onChannelPrevious = {}, onChannelNext = {},
        onOctaveDown = {}, onOctaveUp = {},
        onPlay = {}, onStop = {}, onRecord = {}, onUndo = {}, onPanic = {},
        onPadPressed = {}, onPadReleased = {},
    )
}
