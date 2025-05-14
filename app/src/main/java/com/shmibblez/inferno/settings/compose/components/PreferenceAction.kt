package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText

@Composable
fun PreferenceAction(title: String, action: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = action)
            .padding(
                horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PreferenceConstants.PREFERENCE_VERTICAL_PADDING
            ),
        horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // title
        InfernoText(
            text = title,
            fontSize = 16.sp, // todo: font
            fontColor = Color.White, // todo: theme
        )
        // action chevron
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right_24),
            contentDescription = "go", // todo: string res
            modifier = Modifier.size(24.dp),
            tint = Color.White, // todo: theme
        )
    }
}