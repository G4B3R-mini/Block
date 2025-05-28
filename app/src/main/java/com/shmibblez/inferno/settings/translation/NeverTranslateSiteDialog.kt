package com.shmibblez.inferno.settings.translation

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle

@Composable
fun NeverTranslateSiteDialog(
    websiteUrl: String,
    onDismiss: () -> Unit,
    onConfirmDelete: (String) -> Unit,
) {
    InfernoDialog(onDismiss = onDismiss) {
        // title
        InfernoText(
            text = stringResource(
                R.string.never_translate_site_dialog_title_preference,
                websiteUrl
            ),
            infernoStyle = InfernoTextStyle.Title,
        )

        // cancel/confirm buttons
        Row {
            // cancel
            InfernoOutlinedButton(
                text = stringResource(id = R.string.never_translate_site_dialog_cancel_preference),
                onClick = onDismiss,
                modifier = Modifier.weight(1F)
            )
            // confirm
            InfernoButton(
                text = stringResource(id = R.string.delete_language_file_dialog_positive_button_text),
                onClick = {
                    onConfirmDelete.invoke(websiteUrl)
                    onDismiss.invoke()
                },
                modifier = Modifier.weight(1F),
            )
        }
    }
}