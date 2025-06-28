package com.shmibblez.inferno.toolbar

import android.graphics.drawable.AnimatedVectorDrawable
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.UiConst
import com.shmibblez.inferno.ext.infernoTheme

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
            .fillMaxWidth()
            .height(UiConst.TOOLBAR_HEIGHT + UiConst.TAB_BAR_HEIGHT),
    ) {
        InfernoLoadingSquare(
            modifier = Modifier.align(Alignment.Center),
            size = UiConst.TOOLBAR_HEIGHT,
        )
    }
}

@Composable
fun InfernoLoadingSquare(modifier: Modifier = Modifier, size: Dp) {
    AndroidView(
        modifier = modifier.size(size),
        factory = { context ->
            val imgView = ImageView(context)
            imgView.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            val image = AppCompatResources.getDrawable(
                context, R.drawable.inferno_animated
            ) as AnimatedVectorDrawable
            imgView.setImageDrawable(image)
            image.start()
            imgView
        },
        update = {

        },
    )
}

/**
 * loading screen with centered [InfernoLoadingSquare], fades in when created
 */
@Composable
fun InfernoLoadingScreen(modifier: Modifier = Modifier, loadingSquareSize: Dp = 72.dp) {
    var visible by remember { mutableStateOf(false) }

    // fade in when created
    LaunchedEffect(null) {
        visible = true
    }

    AnimatedVisibility(visible = visible, modifier = modifier, enter = fadeIn(tween(500))) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    LocalContext.current.infernoTheme().value.primaryBackgroundColor.copy(
                        alpha = UiConst.LOADING_ALPHA
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            InfernoLoadingSquare(size = loadingSquareSize)
        }
    }
}