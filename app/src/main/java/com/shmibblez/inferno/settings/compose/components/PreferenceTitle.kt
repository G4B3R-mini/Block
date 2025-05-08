package com.shmibblez.inferno.settings.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.compose.base.InfernoText

@Composable
fun PreferenceTitle(text: String) {
    InfernoText(
        text = text,
        fontSize = 24.sp,
        fontColor = Color.White,
        fontWeight = FontWeight.Bold,
    )
}