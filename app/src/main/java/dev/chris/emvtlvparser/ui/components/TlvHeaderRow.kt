package dev.chris.emvtlvparser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TlvHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HeaderCell("Tag", Modifier.weight(1.5f))
        HeaderCell("L (Hex)", Modifier.weight(1f))
        HeaderCell("Value (Hex)", Modifier.weight(3f))
        HeaderCell("Interpretation", Modifier.weight(3.5f))
    }
    Divider(color = MaterialTheme.colorScheme.onSurface, thickness = 1.dp)
}