/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.topsites

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import mozilla.components.lib.state.ext.observeAsComposableState
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.components.components
import com.shmibblez.inferno.compose.ComposeViewHolder
import com.shmibblez.inferno.home.sessioncontrol.TopSiteInteractor
import com.shmibblez.inferno.perf.StartupTimeline
import com.shmibblez.inferno.wallpapers.WallpaperState

/**
 * View holder for top sites.
 *
 * @param composeView [ComposeView] which will be populated with Jetpack Compose UI content.
 * @param viewLifecycleOwner [LifecycleOwner] to which this Composable will be tied to.
 * @param interactor [TopSiteInteractor] which will have delegated to all user top sites
 * interactions.
 */
class TopSitesViewHolder(
    composeView: ComposeView,
    viewLifecycleOwner: LifecycleOwner,
    private val interactor: TopSiteInteractor,
) : ComposeViewHolder(composeView, viewLifecycleOwner) {

    @Composable
    override fun Content() {
        val topSites =
            components.appStore.observeAsComposableState { state -> state.topSites }.value
        val wallpaperState = components.appStore
            .observeAsComposableState { state -> state.wallpaperState }.value
            ?: WallpaperState.default

        topSites?.let {
            TopSites(
                topSites = it,
                topSiteColors = TopSiteColors.colors(wallpaperState = wallpaperState),
                onTopSiteClick = { topSite ->
                    interactor.onSelectTopSite(topSite, it.indexOf(topSite))
                },
                onTopSiteLongClick = interactor::onTopSiteLongClicked,
                onOpenInPrivateTabClicked = interactor::onOpenInPrivateTabClicked,
                onEditTopSiteClicked = interactor::onEditTopSiteClicked,
                onRemoveTopSiteClicked = interactor::onRemoveTopSiteClicked,
                onSettingsClicked = interactor::onSettingsClicked,
                onSponsorPrivacyClicked = interactor::onSponsorPrivacyClicked,
                onTopSitesItemBound = {
                    StartupTimeline.onTopSitesItemBound(activity = composeView.context as HomeActivity)
                },
            )
        }
    }

    companion object {
        val LAYOUT_ID = View.generateViewId()
    }
}
