package com.shmibblez.inferno.browser.prompts.compose.sub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.feature.prompts.identitycredential.DialogColors


/**
 * List item used to display an IdentityCredential item that supports clicks
 *
 * @param title the Title of the item
 * @param description The Description of the item.
 * @param modifier The modifier to apply to this layout.
 * @param onClick Invoked when the item is clicked.
 * @param beforeItemContent An optional layout to display before the item.
 *
 */
@Composable
internal fun IdentityCredentialItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    colors: DialogColors = DialogColors.default(),
    onClick: () -> Unit,
    beforeItemContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        beforeItemContent?.invoke()

        Column {
            InfernoText(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = colors.title,
                    letterSpacing = 0.15.sp,
                ),
                maxLines = 1,
            )

            InfernoText(
                text = description,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = colors.description,
                    letterSpacing = 0.25.sp,
                ),
                maxLines = 1,
            )
        }
    }
}