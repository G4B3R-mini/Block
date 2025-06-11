package com.shmibblez.inferno.settings.extensions

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
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
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.ui.AddonsManagerAdapterDelegate
import mozilla.components.feature.addons.ui.displayName
import mozilla.components.feature.addons.ui.summary


private val ADDON_ICON_SIZE = 34.dp
private val STAR_SIZE = 12.dp
private val ITEM_HORIZONTAL_PADDING = 16.dp


@Composable
internal fun rememberAddonsManagerState(
    addonManager: AddonManager = LocalContext.current.components.addonManager,
    store: BrowserStore = LocalContext.current.components.core.store,
): MutableState<AddonsManagerState> {
    val state = remember {
        mutableStateOf(
            AddonsManagerState(
                addonManager = addonManager,
                store = store,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

@Composable
internal fun AddonsManager(
    state: AddonsManagerState,
    modifier: Modifier = Modifier,
    onNavToAddon: (addon: Addon) -> Unit,
    onRequestFindMoreAddons: () -> Unit,
    // todo: pass to addonItem, there listen for errors, if error show closeable label
    onRequestLearnMore: (link: AddonsManagerAdapterDelegate.LearnMoreLinks, addon: Addon) -> Unit,
) {
    val store = LocalContext.current.components.core.store

    LazyColumn(
        modifier = modifier,
    ) {

        // restart header
        if (store.state.extensionsProcessDisabled) {
            item {
                AddonHeader(onClick = { state.restartAddons() })
            }
        }

        // installed addons
        if (state.installedAddons.isNotEmpty()) {
            // header
            item {
                AddonSection(stringResource(R.string.mozac_feature_addons_enabled))
            }
            // items
            items(state.installedAddons) { addon ->
                // todo: message bars (error, learn more link) R.string.mozac_feature_addons_status_blocklisted_1
                //  also depending on if installed, disabled, supported, show action button (install (+), uninstall (trash), unsupported (invisible))
                AddonItem(
                    addon = addon,
                    onClick = onNavToAddon,
                )
            }
        }

        // disabled addons
        if (state.disabledAddons.isNotEmpty()) {
            // header
            item {
                AddonSection(stringResource(R.string.mozac_feature_addons_disabled_section))
            }
            // items
            items(state.disabledAddons) { addon ->
                AddonItem(
                    addon = addon,
                    onClick = onNavToAddon,
                )
            }
        }

        // recommended addons
        if (state.recommendedAddons.isNotEmpty()) {
            // header
            item {
                AddonSection(stringResource(R.string.mozac_feature_addons_recommended_section))
            }
            // items
            items(state.recommendedAddons) { addon ->
                AddonItem(
                    addon = addon,
                    onClick = onNavToAddon,
                    // todo: what other addon type requires onRequestLearnMore?
                    onRequestLearnMore = onRequestLearnMore,
                )
            }
        }

        // unsupported addons
        if (state.unsupportedAddons.isNotEmpty()) {
            // header
            item {
                AddonSection(stringResource(R.string.mozac_feature_addons_unavailable_section))
            }
            // items
            items(state.unsupportedAddons) { addon ->
                AddonItem(
                    addon = addon,
                    onClick = onNavToAddon,
                )
            }
        }

        // find more addons button
        item {
            FindMoreAddonsButton(onClick = onRequestFindMoreAddons)
        }

    }
}

@Composable
private fun AddonHeader(onClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = ITEM_HORIZONTAL_PADDING),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        ) {
            // warning icon
            InfernoIcon(
                painter = painterResource(R.drawable.mozac_ic_warning_fill_24),
                contentDescription = "",
                modifier = Modifier.size(18.dp),
            )
            // reset title
            InfernoText(
                text = stringResource(R.string.mozac_feature_extensions_manager_notification_content_text),
                infernoStyle = InfernoTextStyle.Normal,
                fontWeight = FontWeight.Bold,
            )
        }

        // reset description
        InfernoText(
            text = stringResource(R.string.mozac_feature_extensions_manager_notification_content_text),
            infernoStyle = InfernoTextStyle.Normal
        )

        // reset button
        InfernoButton(
            text = stringResource(R.string.mozac_feature_extensions_manager_notification_restart_button),
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }

}

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

@Composable
private fun AddonSection(title: String) {
    InfernoText(
        text = title,
        infernoStyle = InfernoTextStyle.Normal,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    )
}

//@Composable
//private fun UnsupportedAddonSection(unsupportedSize: Int) {
//    InfernoText(
//        text = when (unsupportedSize) {
//            1 -> stringResource(R.string.mozac_feature_addons_unsupported_caption_2)
//            else -> stringResource(
//                R.string.mozac_feature_addons_unsupported_caption_plural_2,
//                "$unsupportedSize",
//            )
//        },
//        infernoStyle = InfernoTextStyle.Normal,
//        fontWeight = FontWeight.Bold,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//    )
//}

@Composable
private fun AddonItem(
    addon: Addon,
    onClick: (addon: Addon) -> Unit,
    onActionClick: ((addon: Addon) -> Unit)? = null,
    // todo: integrate onRequestLearnMore, variable in case error occurred, and callback
    //  in state.install for setting error here,
    //  add x to label left side to close
    onRequestLearnMore: ((link: AddonsManagerAdapterDelegate.LearnMoreLinks, addon: Addon) -> Unit)? = null,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.clickable { onClick.invoke(addon) },
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

        // add icon, show if not installed, else use empty placeholder
        if (!addon.isInstalled()) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_new_24),
                contentDescription = "",
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.CenterVertically)
                    .clickable { onActionClick?.invoke(addon) },
            )
        } else {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.CenterVertically),
            )
        }
    }
}

@Composable
private fun FindMoreAddonsButton(onClick: () -> Unit) {
    InfernoButton(
        text = stringResource(R.string.mozac_feature_addons_find_more_extensions_button_text),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
    )
}