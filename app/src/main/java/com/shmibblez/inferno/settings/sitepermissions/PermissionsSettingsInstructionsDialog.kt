package com.shmibblez.inferno.settings.sitepermissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton

@Composable
fun PermissionsSettingsInstructionsDialog(
    onDismiss: () -> Unit,
    settingName: String,
) {

    val context = LocalContext.current

    fun openSettings() {
        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    InfernoDialog(
        onDismiss = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        InfernoText(text = stringResource(R.string.phone_feature_blocked_by_android))
        InfernoText(text = stringResource(R.string.phone_feature_blocked_intro))
        InfernoText(text = stringResource(R.string.phone_feature_blocked_step_settings))
        InfernoText(text = stringResource(R.string.phone_feature_blocked_step_permissions))
        InfernoText(text = stringResource(R.string.phone_feature_blocked_step_feature, settingName))
        InfernoButton(
            text = stringResource(R.string.phone_feature_go_to_settings),
            onClick = ::openSettings,
        )
    }
}

