package com.shmibblez.inferno.settings.extensions

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.BuildConfig
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.settings.SupportUtils.AMO_HOMEPAGE_FOR_ANDROID
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.AddonsManagerAdapterDelegate

@Composable
fun ExtensionsPage(
    goBack: () -> Unit,
    onNavToAddon: (addon: Addon) -> Unit,
    onNavToBrowser: () -> Unit,
) {
    val context = LocalContext.current
    val addonManagerState by rememberAddonsManagerState(
        addonManager = context.components.addonManager,
    )

    InfernoSettingsPage(
        title = stringResource(R.string.preferences_extensions),
        goBack = goBack,
    ) { edgeInsets ->
        AddonsManager(
            state = addonManagerState,
            modifier = Modifier.padding(edgeInsets),
            onNavToAddon = onNavToAddon,
            onRequestFindMoreAddons = {
                context.components.newTab(url = AMO_HOMEPAGE_FOR_ANDROID)
                onNavToBrowser.invoke()
            },
            onLearnMoreLinkClicked = { link, addon ->
                val url = when (link) {
                    AddonsManagerAdapterDelegate.LearnMoreLinks.BLOCKLISTED_ADDON -> "${BuildConfig.AMO_BASE_URL}/android/blocked-addon/${addon.id}/${addon.version}/"

                    AddonsManagerAdapterDelegate.LearnMoreLinks.ADDON_NOT_CORRECTLY_SIGNED -> SupportUtils.getSumoURLForTopic(
                        context,
                        SupportUtils.SumoTopic.UNSIGNED_ADDONS,
                    )
                }
                context.components.newTab(url = url)
                onNavToBrowser.invoke()
            },
        )
    }
}