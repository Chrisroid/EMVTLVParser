package dev.chris.emvtlvparser.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

@Composable
fun DataCell(text: String, modifier: Modifier = Modifier, color: Color, isMonospace: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
        color = color,
        modifier = modifier,
        lineHeight = 16.sp
    )
}