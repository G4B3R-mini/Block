package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import com.shmibblez.inferno.compose.base.InfernoText

@Composable
fun SwitchPreference(
    text: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PreferenceConstants.PREFERENCE_VERTICAL_PADDING,
            ),
        horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
    ) {
        InfernoText(
            text = text,
            modifier = Modifier
                .weight(1F)
                .alpha(
                    when (enabled) {
                        true -> 1F
                        false -> 0.75F
                    }
                ),
            fontColor = Color.White,
        )
        Switch(
            checked = selected,
            onCheckedChange = onSelectedChange,
            enabled = enabled,
            colors = SwitchColors(
                checkedThumbColor = Color.Red,
                checkedTrackColor = Color.Red.copy(alpha = 0.75F),
                checkedBorderColor = Color.White,
                checkedIconColor = Color.White,
                uncheckedThumbColor = Color.Red,
                uncheckedTrackColor = Color.Red.copy(alpha = 0.75F),
                uncheckedBorderColor = Color.White,
                uncheckedIconColor = Color.White,
                disabledCheckedThumbColor = Color.Gray,
                disabledCheckedTrackColor = Color.Gray.copy(alpha = 0.5F),
                disabledCheckedBorderColor = Color.Gray,
                disabledCheckedIconColor = Color.Gray,
                disabledUncheckedThumbColor = Color.Gray,
                disabledUncheckedTrackColor = Color.Gray.copy(alpha = 0.75F),
                disabledUncheckedBorderColor = Color.Gray,
                disabledUncheckedIconColor = Color.Gray,
            )
        )
    }
}