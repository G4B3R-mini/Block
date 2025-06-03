package com.shmibblez.inferno.browser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.toolbar.InfernoLoadingSquare

@Composable
fun ExternalBrowserComponent() {
    Scaffold { edgeInsets ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(edgeInsets),
        ) {
            InfernoText(
                text = "under construction >:(",
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
            )

            InfernoLoadingSquare(
                size = 74.dp,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}