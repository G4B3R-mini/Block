package com.shmibblez.inferno.settings.sitepermissions

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle

@Composable
internal fun ClearSitePermissionsDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    InfernoDialog(
        onDismiss = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
        ),
    ) {
        InfernoText(
            text = stringResource(R.string.clear_permission),
            infernoStyle = InfernoTextStyle.Title,
        )
        InfernoText(
            text = stringResource(R.string.confirm_clear_permission_site)
        )
        Row {
            InfernoOutlinedButton(
                text = stringResource(android.R.string.cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1F),
            )
            InfernoButton(
                text = stringResource(R.string.clear_permissions_positive),
                onClick = {
                    onConfirm.invoke()
                    onDismiss.invoke()
                },
                modifier = Modifier.weight(1F),
            )
        }
    }
}
