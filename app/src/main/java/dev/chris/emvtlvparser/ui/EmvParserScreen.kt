package dev.chris.emvtlvparser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chris.emvtlvparser.model.TlvItem
import dev.chris.emvtlvparser.singleton.EmvParser
import dev.chris.emvtlvparser.ui.components.TlvDataRow
import dev.chris.emvtlvparser.ui.components.TlvHeaderRow

@Composable
fun EmvParserScreen() {
    // State for the input hex string
    var inputText by remember {
        mutableStateOf("6F188407A0000000031010A50D500B5649534120435245444954")
    }
    // State for the list of parsed TLV items
    var tlvItems by remember { mutableStateOf<List<TlvItem>>(emptyList()) }
    // State for any parsing error messages
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Text(
            text = "EMV TLV Parser",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Input Text Field
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Paste TLV Hex String") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp)
        )

        // Parse Button
        Button(
            onClick = {
                // Clear previous results
                errorMessage = null
                tlvItems = emptyList()

                // Run the parsing
                try {
                    tlvItems = EmvParser.parseTlv(inputText)
                } catch (e: Exception) {
                    errorMessage = "Malformed TLV: ${e.message}"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Parse", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error message display
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }


        // Table Header
        TlvHeaderRow()

        // Scrollable Table Body
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            items(tlvItems) { item ->
                TlvDataRow(item = item)
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}