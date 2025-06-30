package com.shmibblez.inferno.settings.account

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoText

@Composable
internal fun SignOutDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    InfernoDialog(
        onDismiss = onDismiss,
        onConfirm = {
            onConfirm.invoke()
            onDismiss.invoke()
        },
        confirmText = stringResource(R.string.sign_out_disconnect),
    ) {
        item {
            InfernoText(
                text = stringResource(
                    R.string.sign_out_confirmation_message_2,
                    stringResource(R.string.app_name),
                )
            )
        }
    }
}