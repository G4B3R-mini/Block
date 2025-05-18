package com.shmibblez.inferno.settings.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun EditThemeDialog(
    baseTheme: InfernoTheme,
    onDismiss: () -> Unit,
    onSaveTheme: (InfernoTheme) -> Unit,
) {
    var theme by remember { mutableStateOf(baseTheme) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
        ),
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = theme.secondaryBackgroundColor),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1F)
                ) {
                    // todo: add all items (name text field, color picker, preview) here
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                // todo: add buttons to save/cancel here
            }
        }
    }
}

@Composable
private fun ColorEditor(color: Color, onChangeColor: (Color) -> Unit) {
    Row {
        // todo: show color name, textfield to edit color, and color square at the end
        //  textfield should only allow 0-9, a-f, A-F
        //  show error if color not properly formatted, in error show hint: "Color should be in format #RRGGBBAA
        //  parse hex as color and then call onChangeColor once successful
    }
}

