package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// todo (10):
//   - make layout identical to toolbar, but glean
/**
 * placeholder while store loads
 */
@Composable
internal fun PlaceholderBrowserToolbar() {
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize(),
    )
}