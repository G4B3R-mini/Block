package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle

@Composable
fun PreferenceTitle(text: String) {
    InfernoText(
        text = text,
        infernoStyle = InfernoTextStyle.Title,
        modifier = Modifier.padding(
            horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
            vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
        )
    )
}