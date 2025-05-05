package com.shmibblez.inferno.toolbar

import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.ComponentDimens
import kotlinx.coroutines.delay

// todo (10):
//   - make layout identical to toolbar, but glean
/**
 * placeholder while store loads
 */
@Composable
internal fun PlaceholderBrowserToolbar() {
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.inferno_animated)
    Log.d("PlaceholderBrowserToolb", "image.totalDuration: ${image.totalDuration}")
    var atEnd by remember { mutableStateOf(false) }
    LaunchedEffect(null) {
        while (true) {
            delay(4000)
            atEnd = !atEnd
        }
    }
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
            .height(ComponentDimens.TOOLBAR_HEIGHT),
    ) {
        AndroidView(
            modifier = Modifier
                .size(ComponentDimens.TOOLBAR_HEIGHT * 0.75F)
                .align(Alignment.Center),
            factory = { context ->
                val imgView = ImageView(context)
                imgView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                val image = AppCompatResources.getDrawable(context, R.drawable.inferno_animated) as AnimatedVectorDrawable
                imgView.setImageDrawable(image)
                image.start()
                imgView
            },
            update = {

            },
        )
    }
}