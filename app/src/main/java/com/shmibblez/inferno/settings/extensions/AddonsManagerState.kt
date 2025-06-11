package com.shmibblez.inferno.settings.extensions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shmibblez.inferno.history.ConsecutiveUniqueJobHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import mozilla.components.browser.state.action.ExtensionsProcessAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.AddonManager
import mozilla.components.support.base.feature.LifecycleAwareFeature

internal class AddonsManagerState(
    val addonManager: AddonManager,
    val store: BrowserStore,
) : LifecycleAwareFeature {

    enum class AddonManagerTask {
        REFRESH, RESET, INSTALL, UNINSTALL,
    }

    val scope = MainScope()
    private val taskManager = ConsecutiveUniqueJobHandler<AddonManagerTask>(scope)
    var isBusy by mutableStateOf(false)

    // addons lists
    private var allAddons by mutableStateOf<List<Addon>>(emptyList())
    val installedAddons = mutableListOf<Addon>()
    val recommendedAddons = mutableListOf<Addon>()
    val disabledAddons = mutableListOf<Addon>()
    val unsupportedAddons = mutableListOf<Addon>()


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

    private fun Addon.inUnsupportedSection() = isInstalled() && !isSupported()
    private fun Addon.inRecommendedSection() = !isInstalled()
    private fun Addon.inInstalledSection() = isInstalled() && isSupported() && isEnabled()
    private fun Addon.inDisabledSection() = isInstalled() && isSupported() && !isEnabled()

    private fun installAddon(addon: Addon) {
        if (addon.isSupported() && !addon.isInstalled()) return
        addonManager.installAddon(
            url = addon.downloadUrl,
            onSuccess = { refreshAddons() },
            onError = { error ->
                // todo
            },
        )
    }

    private fun uninstallAddon(addon: Addon) {
        if (!addon.isInstalled()) return
        addonManager.uninstallAddon(
            addon = addon,
            onSuccess = { refreshAddons() },
            onError = { id, error ->
                // todo
            },
        )
    }

    private suspend fun refreshAddonsSus() {
        allAddons = addonManager.getAddons()
        installedAddons.clear()
        recommendedAddons.clear()
        disabledAddons.clear()
        unsupportedAddons.clear()
        allAddons.forEach { addon ->
            when {
                addon.inInstalledSection() -> installedAddons.add(addon)
                addon.inRecommendedSection() -> recommendedAddons.add(addon)
                addon.inDisabledSection() -> disabledAddons.add(addon)
                addon.inUnsupportedSection() -> unsupportedAddons.add(addon)
            }
        }
    }

    private fun refreshAddons() {
        taskManager.processTask(
            type = AddonManagerTask.REFRESH,
            task = { refreshAddonsSus() },
            onComplete = { isBusy = !it },
        )
    }

    fun restartAddons() {
        taskManager.processTask(
            type = AddonManagerTask.RESET,
            task = {
                store.dispatch(ExtensionsProcessAction.EnabledAction)
                refreshAddonsSus()
            }
        )
    }

    override fun start() {
        refreshAddons()
    }

    override fun stop() {
        scope.cancel()
    }

}