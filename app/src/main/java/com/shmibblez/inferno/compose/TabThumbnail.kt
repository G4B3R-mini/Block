/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.compose

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.theme.FirefoxTheme
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.createTab
import mozilla.components.concept.base.images.ImageLoadRequest
import com.shmibblez.inferno.R

private const val FALLBACK_ICON_SIZE = 36

/**
 * Thumbnail belonging to a [tab]. If a thumbnail is not available, the favicon
 * will be displayed until the thumbnail is loaded.
 *
 * @param tab The given [TabSessionState] to render a thumbnail for.
 * @param size Size of the thumbnail.
 * @param modifier [Modifier] used to draw the image content.
 * @param backgroundColor [Color] used for the background of the favicon.
 * @param contentDescription Text used by accessibility services
 * to describe what this image represents.
 * @param contentScale [ContentScale] used to draw image content.
 * @param alignment [Alignment] used to draw the image content.
 */
@Composable
fun TabThumbnail(
    tab: TabSessionState,
    size: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black, // FirefoxTheme.colors.layer2,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.FillWidth,
    alignment: Alignment = Alignment.TopCenter,
) {
    Box(
//        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        ThumbnailImage(
            modifier = Modifier.fillMaxSize(),
            request = ImageLoadRequest(
                id = tab.id,
                size = size,
                isPrivate = tab.content.private,
            ),
            contentScale = contentScale,
            alignment = alignment,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {

                val icon = when (tab.content.url) {
                    "inferno:home" -> BitmapFactory.decodeResource(
                        LocalContext.current.resources, R.drawable.inferno
                    )

                    "inferno:privatebrowsing" -> BitmapFactory.decodeResource(
                        LocalContext.current.resources, R.drawable.ic_private_browsing_24
                    )

                    else -> tab.content.icon
                }

                if (icon != null) {
                    icon.prepareToDraw()
                    Image(
                        bitmap = icon.asImageBitmap(),
                        contentDescription = contentDescription,
                        modifier = Modifier
                            .size(FALLBACK_ICON_SIZE.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.FillWidth,
                    )
                } else {
                    Favicon(
                        url = tab.content.url,
                        size = FALLBACK_ICON_SIZE.dp,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ThumbnailCardPreview() {
    FirefoxTheme {
        TabThumbnail(
            tab = createTab(url = "www.mozilla.com", title = "Mozilla"),
            size = 108,
            modifier = Modifier
                .size(108.dp, 80.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
    }
}
