package com.shmibblez.inferno.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle

@Composable
fun ConfirmDeleteSelectedDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    InfernoDialog(
        onDismiss = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        InfernoText(
            text = stringResource(R.string.delete_from_history),
            infernoStyle = InfernoTextStyle.Normal,
        )

        // cancel / confirm buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // cancel
            InfernoOutlinedButton(
                text = stringResource(android.R.string.cancel),
                modifier = Modifier.weight(1F),
                onClick = onDismiss,
            )
            // confirm
            InfernoButton(
                text = stringResource(android.R.string.ok),
                modifier = Modifier.weight(1F),
                onClick = onConfirm,
            )
        }
    }
}