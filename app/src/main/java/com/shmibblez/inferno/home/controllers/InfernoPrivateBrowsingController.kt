package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.home.privatebrowsing.controller.DefaultPrivateBrowsingController
import com.shmibblez.inferno.home.privatebrowsing.controller.PrivateBrowsingController

/**
 * todo: reference [DefaultPrivateBrowsingController]
 */
class InfernoPrivateBrowsingController(
    private val activity: HomeActivity,
    private val appStore: AppStore,
): PrivateBrowsingController {
    override fun handleLearnMoreClicked() {
        // TODO("Not yet implemented")
    }

    override fun handlePrivateModeButtonClicked(newMode: BrowsingMode) {
        // TODO("Not yet implemented")
    }

}