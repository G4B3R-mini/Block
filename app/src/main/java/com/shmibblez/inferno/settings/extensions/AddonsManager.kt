package com.shmibblez.inferno.settings.extensions

import androidx.annotation.FloatRange
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.ui.AddonsManagerAdapter
import mozilla.components.feature.addons.ui.displayName
import mozilla.components.feature.addons.ui.summary
import mozilla.components.support.base.feature.LifecycleAwareFeature

@Composable
internal fun rememberAddonsManagerState(
    addonManager: AddonManager = LocalContext.current.components.addonManager,
): MutableState<AddonsManagerState> {
    val state = remember {
        mutableStateOf(
            AddonsManagerState(
                addonManager = addonManager,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

internal class AddonsManagerState(
    val addonManager: AddonManager,
) : LifecycleAwareFeature {

    var addons by mutableStateOf<List<Addon>>(emptyList())


    /**
     * todo: from [AddonsManagerAdapter]
     * when addon clicked, new page (details page)
     * if not installed, show details
     * if installed, show details & settings above, when settings clicked show dialog
     * can make component for details since will be shown in both pages
     */
//    fun onAddonItemClicked(addon: Addon) {
//        if (addon.isInstalled()) {
//            showInstalledAddonDetailsFragment(addon)
//        } else {
//            showDetailsFragment(addon)
//        }
//    }

    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

}

private const val VIEW_HOLDER_TYPE_SECTION = 0
private const val VIEW_HOLDER_TYPE_NOT_YET_SUPPORTED_SECTION = 1
private const val VIEW_HOLDER_TYPE_ADDON = 2
private const val VIEW_HOLDER_TYPE_FOOTER = 3
private const val VIEW_HOLDER_TYPE_HEADER = 4

private val ADDON_ICON_SIZE = 34.dp

@Composable
internal fun AddonsManager(
    state: AddonsManagerState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
    ) {

    }
}

private val STAR_SIZE = 12.dp

@Composable
private fun StarRating(@FloatRange(0.0, 5.0) rating: Float, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 0..4) {
            val percent = (rating - i).coerceIn(0F, 1F)
            if (percent < 1) {
                PartialStar(percent)
            } else {
                FullStar()
            }
        }
    }
}

@Composable
private fun FullStar() {
    InfernoIcon(
        painter = painterResource(R.drawable.ic_star_24),
        contentDescription = "",
        modifier = Modifier.size(STAR_SIZE),
        tint = LocalContext.current.infernoTheme().value.primaryActionColor,
    )
}

@Composable
private fun PartialStar(@FloatRange(0.0, 1.0) percent: Float) {
    Box(
        modifier = Modifier.size(STAR_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        InfernoIcon(
            painter = painterResource(R.drawable.ic_star_24),
            contentDescription = "",
            modifier = Modifier.size(STAR_SIZE),
            tint = LocalContext.current.infernoTheme().value.secondaryIconColor,
        )
        if (percent > 0) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_star_24),
                contentDescription = "",
                modifier = Modifier
                    .size(STAR_SIZE)
                    .graphicsLayer {
                        clip = true
                        shape = object : Shape {
                            override fun createOutline(
                                size: Size,
                                layoutDirection: LayoutDirection,
                                density: Density,
                            ): Outline {
                                return Outline.Rectangle(
                                    Rect(
                                        topLeft = Offset(0F, 0F),
                                        bottomRight = Offset(size.width * percent, size.height),
                                    )
                                )
                            }

                        }
                    },
                tint = LocalContext.current.infernoTheme().value.primaryActionColor,
            )
        }
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
private data class Section(@StringRes val title: Int, val visibleDivider: Boolean = true)

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
private data class NotYetSupportedSection(@StringRes val title: Int)

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
private object FooterSection

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
private object HeaderSection

@Composable
private fun AddonSection(section: Section) {
    InfernoText(
        text = stringResource(section.title),
        infernoStyle = InfernoTextStyle.Normal,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    )
}

@Composable
private fun UnsupportedAddonSection(section: NotYetSupportedSection, unsupportedSize: Int) {
    InfernoText(
        text = when (unsupportedSize) {
            1 -> stringResource(R.string.mozac_feature_addons_unsupported_caption_2)
            else -> stringResource(
                R.string.mozac_feature_addons_unsupported_caption_plural_2,
                "$unsupportedSize",
            )
        },
        infernoStyle = InfernoTextStyle.Normal,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    )
}

// todo: header section and footer section (find more extensions)

@Composable
private fun AddonItem(addon: Addon) {
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        verticalAlignment = Alignment.Top,
    ) {
        // addon icon
        when (addon.icon) {
            null -> {
                InfernoIcon(
                    painter = painterResource(R.drawable.ic_globe_24),
                    contentDescription = "",
                    modifier = Modifier.size(ADDON_ICON_SIZE),
                )
            }

            else -> {
                Image(
                    bitmap = addon.icon!!.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier.size(ADDON_ICON_SIZE),
                )
            }
        }

        // addon name, description, and reviews
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            // addon name & private mode availability
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InfernoText(
                    text = addon.displayName(context),
                    infernoStyle = InfernoTextStyle.Normal,
                )
                // available in private mode
                if (addon.isAllowedInPrivateBrowsing()) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color = LocalContext.current.infernoTheme().value.primaryActionColor,
                                shape = CircleShape,
                            )
                    ) {
                        InfernoIcon(
                            painter = painterResource(R.drawable.ic_private_browsing),
                            contentDescription = "",
                            modifier = Modifier.size(10.dp),
                        )
                    }
                }
            }
            // addon description
            addon.summary(context)?.let {
                InfernoText(
                    text = it,
                    infernoStyle = InfernoTextStyle.Subtitle,
                )
            }
            // star rating & number of reviews
            addon.rating?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)) {
                    StarRating(it.average)
                    InfernoText(
                        text = stringResource(
                            R.string.mozac_feature_addons_user_rating_count_2, it.average
                        ),
                        infernoStyle = InfernoTextStyle.Subtitle,
                    )
                }
            }
        }

        // add icon
        InfernoIcon(
            painter = painterResource(R.drawable.ic_new_24),
            contentDescription = "",
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.CenterVertically),
        )
    }
}