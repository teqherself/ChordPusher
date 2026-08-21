package com.gendemik.chordpusher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gendemik.chordpusher.ui.theme.ChordPusherColors

/**
 * Reusable command deck button used for transport controls,
 * mode selectors, track/channel navigation, etc.
 */
@Composable
fun CommandButton(
    label: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    accentColor: Color = ChordPusherColors.ButtonNeutral,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(4.dp)
    val bgColor = if (active) ChordPusherColors.ButtonActive else ChordPusherColors.SurfaceVariant
    val textColor = if (active) Color.Black else ChordPusherColors.TextPrimary
    val borderColor = if (active) ChordPusherColors.ButtonActive else accentColor

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun CommandButtonPreview() {
    CommandButton(label = "PLAY", active = true, accentColor = ChordPusherColors.ButtonPlay)
}
