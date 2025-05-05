package com.shmibblez.inferno.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.toolbar.InfernoLoadingSquare

@Composable
fun InfernoLoadingComponent() {
    Box(
        modifier = Modifier
            .background(Color(0xFF111111))
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        InfernoLoadingSquare(
            modifier = Modifier.align(Alignment.Center),
            size = 128.dp,
        )
    }
}