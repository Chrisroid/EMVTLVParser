package dev.chris.emvtlvparser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chris.emvtlvparser.model.TlvItem

@Composable
fun TlvDataRow(item: TlvItem) {
    val rowColor = if (item.isError) {
        MaterialTheme.colorScheme.errorContainer
    } else if (item.level == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val contentColor = if (item.isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowColor)
            .padding(
                start = (12 + item.level * 16).dp, // Indentation for nested items
                end = 12.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.Top
    ) {
        DataCell(item.tag, Modifier.weight(1.5f), contentColor, isMonospace = true)
        DataCell(item.rawLength, Modifier.weight(1f), contentColor, isMonospace = true)
        DataCell(item.value, Modifier.weight(3f), contentColor, isMonospace = true)
        DataCell(item.interpretation, Modifier.weight(3.5f), contentColor)
    }
}