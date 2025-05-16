package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shmibblez.inferno.compose.base.InfernoText

@Composable
fun PreferenceSpacer() {
    InfernoText(
        text = "   ",
        modifier = Modifier.padding(
            horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING,
            vertical = PreferenceConstants.PREFERENCE_VERTICAL_PADDING,
        ),
    )
}