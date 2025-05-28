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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.compose.base.InfernoSwitch
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
                horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
            ),
        horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
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
        InfernoSwitch(
            checked = selected,
            onCheckedChange = onSelectedChange,
            enabled = enabled,
        )
    }
}