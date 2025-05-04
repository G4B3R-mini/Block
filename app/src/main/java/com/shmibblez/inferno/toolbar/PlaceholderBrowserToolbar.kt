package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.shmibblez.inferno.browser.ComponentDimens
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R

// todo (10):
//   - make layout identical to toolbar, but glean
/**
 * placeholder while store loads
 */
@Composable
internal fun PlaceholderBrowserToolbar() {
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.inferno_animated)
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
            .height(ComponentDimens.TOOLBAR_HEIGHT),
    ) {
        Image(
            painter = rememberAnimatedVectorPainter(image, atEnd = false),
            contentDescription = stringResource(R.string.mozac_browser_toolbar_progress_loading),
            modifier = Modifier.size(ComponentDimens.TOOLBAR_HEIGHT * 0.75F)
                .align(Alignment.Center),
        )
    }
}