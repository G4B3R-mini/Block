package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.ext.selectLastNormalTab
import com.shmibblez.inferno.ext.selectLastPrivateTab
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.home.privatebrowsing.controller.DefaultPrivateBrowsingController
import com.shmibblez.inferno.home.privatebrowsing.controller.PrivateBrowsingController
import com.shmibblez.inferno.settings.SupportUtils

/**
 * based off [DefaultPrivateBrowsingController]
 */
class InfernoPrivateBrowsingController(
    private val activity: HomeActivity,
    private val appStore: AppStore,
) : PrivateBrowsingController {
    override fun handleLearnMoreClicked() {
        val learnMoreURL =
            SupportUtils.getGenericSumoURLForTopic(SupportUtils.SumoTopic.PRIVATE_BROWSING_MYTHS) +
                    "?as=u&utm_source=inproduct"

        activity.components.newTab(
            url = learnMoreURL,
            private = true,
        )
    }

    override fun handlePrivateModeButtonClicked(newMode: BrowsingMode) {
        if (newMode == BrowsingMode.Private) {
            activity.settings().incrementNumTimesPrivateModeOpened()
        }

        appStore.dispatch(
            AppAction.ModeChange(newMode),
        )

        when (newMode) {
            BrowsingMode.Normal -> activity.components.selectLastNormalTab()
            BrowsingMode.Private -> activity.components.selectLastPrivateTab()
        }
    }
}