package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.ext.infernoTheme

@Composable
fun InfernoDialog(
    onDismiss: () -> Unit,
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false,
    ),
    colors: CardColors = CardColors(
        containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        contentColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        disabledContainerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        disabledContentColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties,
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = colors,
            content = content,
        )
    }
}