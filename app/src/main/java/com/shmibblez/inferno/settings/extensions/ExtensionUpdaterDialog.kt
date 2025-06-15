package com.shmibblez.inferno.settings.extensions

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.feature.addons.update.AddonUpdater
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import mozilla.components.feature.addons.ui.toLocalizedString
import java.text.DateFormat

@Composable
internal fun ExtensionUpdaterDialog(
    updateAttempt: AddonUpdater.UpdateAttempt,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    InfernoDialog(
        onDismiss = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true,
        )
    ) {
//        .setTitle(R.string.mozac_feature_addons_updater_dialog_title)
//        .setMessage(getDialogMessage(context))
//        .show()
//        .withCenterAlignedButtons()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING)
        ) {
            // title
            item {
                InfernoText(
                    text = stringResource(R.string.mozac_feature_addons_updater_dialog_title),
                    fontWeight = FontWeight.Bold,
                )
            }
            // description
            item {
                InfernoText(
                    text = updateAttempt.getDialogMessage(context)
                )
            }
            // dismiss button
            item {
                InfernoOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(android.R.string.ok),
                    onClick = onDismiss,
                )
            }
        }
    }
}

private fun AddonUpdater.UpdateAttempt.getDialogMessage(context: Context): String {
    val statusString = status.toLocalizedString(context)
    val dateString = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(date)
    val lastAttemptLabel =
        context.getString(R.string.mozac_feature_addons_updater_dialog_last_attempt)
    val statusLabel = context.getString(R.string.mozac_feature_addons_updater_dialog_status)
    return "$lastAttemptLabel $dateString \n $statusLabel $statusString ".trimMargin()
}