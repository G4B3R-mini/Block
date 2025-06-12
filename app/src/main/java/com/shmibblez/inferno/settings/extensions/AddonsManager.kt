package com.shmibblez.inferno.settings.extensions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.StarRating
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.toolbar.InfernoLoadingSquare
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.AddonsManagerAdapterDelegate
import mozilla.components.feature.addons.ui.displayName
import mozilla.components.feature.addons.ui.summary
import java.text.NumberFormat
import java.util.Locale

private val ADDON_ICON_SIZE = 28.dp
private val ITEM_HORIZONTAL_PADDING = 16.dp

// todo: add loading indicator, could be progress circle for add button,
//  or snackbar
//  error may be occurring, also listen for errors in AddonsManagerState
@Composable
internal fun AddonsManager(
    state: AddonsManagerState,
    modifier: Modifier = Modifier,
    onNavToAddon: (addon: Addon) -> Unit,
    onRequestFindMoreAddons: () -> Unit,
    // todo: pass to addonItem, there listen for errors, if error show closeable label
    onLearnMoreLinkClicked: (link: AddonsManagerAdapterDelegate.LearnMoreLinks, addon: Addon) -> Unit,
) {
    val store = LocalContext.current.components.core.store

    when (state.initialLoad) {
        true -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                InfernoLoadingSquare(size = 72.dp)
            }
        }

        false -> {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
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
                            onActionClick = { updatedAddon, onSuccess, onError ->
                                state.uninstallAddon(
                                    updatedAddon, { onSuccess?.invoke(null) }, onError
                                )
                            },
                            onLearnMoreLinkClicked = onLearnMoreLinkClicked,
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
                            onLearnMoreLinkClicked = onLearnMoreLinkClicked,
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
                            onActionClick = { updatedAddon, onSuccess, onError ->
                                state.installAddon(updatedAddon, onSuccess, onError)
                            },
                            onLearnMoreLinkClicked = onLearnMoreLinkClicked,
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
                            onLearnMoreLinkClicked = onLearnMoreLinkClicked,
                        )
                    }
                }

                // find more addons button
                item {
                    FindMoreAddonsButton(onClick = onRequestFindMoreAddons)
                }
            }
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
private fun AddonSection(title: String) {
    InfernoText(
        text = title,
        infernoStyle = InfernoTextStyle.Normal,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = ITEM_HORIZONTAL_PADDING),
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
    onActionClick: ((
        addon: Addon,
        onSuccess: ((addon: Addon?) -> Unit)?,
        onError: ((Throwable) -> Unit)?,
    ) -> Unit)? = null,
    // todo: integrate onRequestLearnMore, variable in case error occurred, and callback
    //  in state.install for setting error here,
    //  add x to label left side to close
    onLearnMoreLinkClicked: (link: AddonsManagerAdapterDelegate.LearnMoreLinks, addon: Addon) -> Unit,
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<Throwable?>(null) }

    Column(
        modifier = Modifier.padding(horizontal = ITEM_HORIZONTAL_PADDING),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
    ) {
        AddonMessageBars(addon, onLearnMoreLinkClicked)

        // addon content
        Row(
            modifier = Modifier.clickable { onClick.invoke(addon) },
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.Top,
        ) {
            // addon icon
            Box(
                modifier = Modifier
                    .size(ADDON_ICON_SIZE + 12.dp)
                    .background(
                        color = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                        shape = MaterialTheme.shapes.extraSmall,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when (addon.provideIcon()) {
                    null -> {
                        InfernoIcon(
                            painter = painterResource(R.drawable.ic_globe_24),
                            contentDescription = "",
                            modifier = Modifier.size(ADDON_ICON_SIZE),
                        )
                    }

                    else -> {
                        Image(
                            bitmap = addon.provideIcon()!!.asImageBitmap(),
                            contentDescription = "",
                            modifier = Modifier.size(ADDON_ICON_SIZE),
                        )
                    }
                }
            }

            // addon name, description, and reviews
            Column(
                modifier = Modifier.weight(1F),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
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
                    // available in private mode icon
                    if (addon.isAllowedInPrivateBrowsing()) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    color = LocalContext.current.infernoTheme().value.primaryActionColor,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            InfernoIcon(
                                painter = painterResource(R.drawable.ic_private_browsing_24),
                                contentDescription = "",
                                modifier = Modifier.size(14.dp),
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StarRating(it.average)
                        val reviews =
                            NumberFormat.getNumberInstance(Locale.getDefault()).format(it.reviews)
                        InfernoText(
                            text = stringResource(
                                R.string.mozac_feature_addons_user_rating_count_2, reviews
                            ),
                            infernoStyle = InfernoTextStyle.Subtitle,
                        )
                    }
                }
            }

            // action icon
            when {
                // if not installed show add
                !addon.isInstalled() -> {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_new_24),
                        contentDescription = "",
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                onActionClick?.invoke(addon, { error = null }, { error = it })
                            },
                    )
                }
                // if installed show uninstall
                addon.isInstalled() -> {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_delete_24),
                        contentDescription = "",
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                onActionClick?.invoke(addon, { error = null }, { error = it })
                            },
                    )
                }
                // else show placeholder
                else -> {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterVertically),
                    )
                }
            }

        }
    }
}

@Composable
private fun FindMoreAddonsButton(onClick: () -> Unit) {
    InfernoButton(
        text = stringResource(R.string.mozac_feature_addons_find_more_extensions_button_text),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        shape = RectangleShape,
    )
}