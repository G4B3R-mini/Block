/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.compose

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.shmibblez.inferno.R
import mozilla.components.browser.icons.IconRequest
import mozilla.components.browser.icons.compose.Placeholder
import mozilla.components.browser.icons.compose.WithIcon
//import com.shmibblez.inferno.mozillaAndroidComponents.base.compose.utils.inComposePreview
import com.shmibblez.inferno.components.components
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.theme.FirefoxTheme

/**
 * Load and display the favicon of a particular website.
 *
 * @param url Website URL for which the favicon will be shown.
 * @param size [Dp] height and width of the image to be loaded.
 * @param modifier [Modifier] to be applied to the layout.
 * @param isPrivate Whether or not a private request (like in private browsing) should be used to
 * download the icon (if needed).
 * @param imageUrl Optional image URL to create an [IconRequest.Resource] from.
 */
@Composable
fun Favicon(
    url: String,
    size: Dp,
    modifier: Modifier = Modifier,
    isPrivate: Boolean = false,
    imageUrl: String? = null,
) {

    val iconResource = imageUrl?.let {
        IconRequest.Resource(
            url = imageUrl,
            type = IconRequest.Resource.Type.FAVICON,
        )
    }
    val context = LocalContext.current
    val isHomepage = url == SupportUtils.INFERNO_HOME_URL || url == SupportUtils.INFERNO_HOME_URL_2
    val isPrivateHomepage =
        url == SupportUtils.INFERNO_PRIVATE_HOME_URL || url == SupportUtils.INFERNO_PRIVATE_HOME_URL_2
    val icon = when {
        isHomepage -> AppCompatResources.getDrawable(context, R.drawable.inferno)?.toBitmap()
            ?.asImageBitmap()

        isPrivateHomepage -> AppCompatResources.getDrawable(context, R.drawable.ic_private_browsing)
            ?.toBitmap()?.asImageBitmap()

        else -> null
    }
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = "",
            modifier = Modifier.size(size),
        )
    } else {
        components.core.icons.LoadableImage(
            url = url,
            iconResource = iconResource,
            isPrivate = isPrivate,
            iconSize = size.toIconRequestSize(),
        ) {
            Placeholder {
                FaviconPlaceholder(
                    size = size,
                    modifier = modifier,
                )
            }

            WithIcon { icon ->
                Image(
                    painter = icon.painter,
                    contentDescription = null,
                    modifier = modifier
                        .size(size)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

/**
 * Placeholder used while the Favicon image is loading.
 *
 * @param size [Dp] height and width of the image.
 * @param modifier [Modifier] allowing to control among others the dimensions and shape of the image.
 */
@Composable
fun FaviconPlaceholder(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    FirefoxTheme {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    color = FirefoxTheme.colors.layer2,
                ),
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun FaviconPreview() {
    FirefoxTheme {
        Box(Modifier.background(FirefoxTheme.colors.layer1)) {
            Favicon(
                url = "www.mozilla.com",
                size = 64.dp,
            )
        }
    }
}

@Composable
private fun Dp.toIconRequestSize() = when {
    value <= dimensionResource(IconRequest.Size.DEFAULT.dimen).value -> IconRequest.Size.DEFAULT
    value <= dimensionResource(IconRequest.Size.LAUNCHER.dimen).value -> IconRequest.Size.LAUNCHER
    else -> IconRequest.Size.LAUNCHER_ADAPTIVE
}
