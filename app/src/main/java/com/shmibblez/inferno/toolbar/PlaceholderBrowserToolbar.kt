package com.shmibblez.inferno.toolbar

import android.graphics.drawable.AnimatedVectorDrawable
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.UiConst

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