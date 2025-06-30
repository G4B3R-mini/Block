package com.shmibblez.inferno.settings.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoTextStyle

@Composable
internal fun EditDeviceNameDialog(
    onDismiss: () -> Unit,
    onUpdateName: (String) -> Boolean,
    initialDeviceName: String,
) {
    val context = LocalContext.current
    var deviceName by remember { mutableStateOf(initialDeviceName) }
    var error by remember { mutableStateOf<String?>(null) }

    fun hasError(name: String = deviceName): Boolean {
        // todo: how to determine error?
        if (name.trim().isEmpty()) {
            error = context.getString(R.string.empty_device_name_error)
            return true
        }
        error = null
        return false
    }

    InfernoDialog(
        onDismiss = onDismiss,
        onConfirm = {
            val name = deviceName
            if (!hasError(name)) {
                onUpdateName.invoke(deviceName)
                onDismiss.invoke()
            }
        },
        confirmEnabled = error == null,
    ) {
        item {
            InfernoText(
                text = stringResource(R.string.preferences_sync_device_name),
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            InfernoOutlinedTextField(
                value = deviceName,
                onValueChange = {
                    deviceName = it
                    hasError()
                },
                isError = error != null,
                supportingText = {
                    error?.let {
                        InfernoText(text = it, infernoStyle = InfernoTextStyle.Error)
                    }
                }
            )
        }
    }
}