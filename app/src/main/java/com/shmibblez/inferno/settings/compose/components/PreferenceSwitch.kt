package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.infernoTheme

@Composable
fun PreferenceSwitch(
    modifier: Modifier = Modifier,
    text: String,
    summary: String? = null,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PreferenceConstants.PREFERENCE_VERTICAL_PADDING,
            ),
        horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // title
            InfernoText(
                text = text,
                fontColor = context.infernoTheme().value.let {
                    when (enabled) {
                        true -> it.primaryTextColor
                        false -> it.secondaryTextColor
                    }
                },
            )
            // description
            if (enabled && summary != null) {
                InfernoText(
                    text = summary,
                    fontSize = 12.sp,
                    fontColor = context.infernoTheme().value.secondaryTextColor,
                )
            }
        }
        Switch(
            checked = selected,
            onCheckedChange = onSelectedChange,
            enabled = enabled,
            colors = SwitchColors(
                checkedThumbColor = context.infernoTheme().value.primaryActionColor,
                checkedTrackColor = context.infernoTheme().value.secondaryActionColor,
                checkedBorderColor = context.infernoTheme().value.primaryActionColor,
                checkedIconColor = context.infernoTheme().value.primaryIconColor,
                uncheckedThumbColor = context.infernoTheme().value.primaryActionColor,
                uncheckedTrackColor = context.infernoTheme().value.secondaryActionColor,
                uncheckedBorderColor = context.infernoTheme().value.primaryActionColor,
                uncheckedIconColor = context.infernoTheme().value.primaryIconColor,
                disabledCheckedThumbColor = context.infernoTheme().value.secondaryTextColor,    // .secondaryBackgroundColor,
                disabledCheckedTrackColor = context.infernoTheme().value.secondaryTextColor,    // .secondaryBackgroundColor,
                disabledCheckedBorderColor = context.infernoTheme().value.secondaryTextColor,   // .secondaryBackgroundColor,
                disabledCheckedIconColor = context.infernoTheme().value.secondaryTextColor,     // .secondaryIconColor,
                disabledUncheckedThumbColor = context.infernoTheme().value.secondaryTextColor,  // .secondaryBackgroundColor,
                disabledUncheckedTrackColor = context.infernoTheme().value.secondaryTextColor,  // .secondaryBackgroundColor,
                disabledUncheckedBorderColor = context.infernoTheme().value.secondaryTextColor, // .secondaryBackgroundColor,
                disabledUncheckedIconColor = context.infernoTheme().value.secondaryTextColor,   // .secondaryIconColor,
            )
        )
    }
}