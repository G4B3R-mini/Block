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
import mozilla.components.feature.addons.ui.AddonsManagerAdapter
import mozilla.components.support.base.feature.LifecycleAwareFeature

internal class AddonsManagerState(
    val addonManager: AddonManager,
    val store: BrowserStore,
) : LifecycleAwareFeature {

    enum class AddonManagerTask {
        REFRESH, RESET
    }

    val scope = MainScope()
    private val taskManager = ConsecutiveUniqueJobHandler<AddonManagerTask>(scope)
    private var isBusy by mutableStateOf(false)
    var initialLoad by mutableStateOf(true)
        private set
    private var pendingInstallationTask by mutableStateOf(false)

    // addons lists
    private var allAddons by mutableStateOf<List<Addon>>(emptyList())
    var installedAddons by mutableStateOf<List<Addon>>(emptyList())
        private set
    var recommendedAddons by mutableStateOf<List<Addon>>(emptyList())
        private set
    var disabledAddons by mutableStateOf<List<Addon>>(emptyList())
        private set
    var unsupportedAddons by mutableStateOf<List<Addon>>(emptyList())
        private set


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

    fun installAddon(
        addon: Addon,
        onSuccess: ((addon: Addon) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
    ) {
        if (!pendingInstallationTask && addon.isSupported() && !addon.isInstalled()) return
        pendingInstallationTask = true
        addonManager.installAddon(
            url = addon.downloadUrl,
            onSuccess = {
                refreshAddons { pendingInstallationTask = false }
                onSuccess?.invoke(it)
            },
            onError = {
                pendingInstallationTask = false
                onError?.invoke(it)
            },
        )
    }

    fun uninstallAddon(
        addon: Addon,
        onSuccess: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
    ) {
        if (!pendingInstallationTask && !addon.isInstalled()) return
        pendingInstallationTask = true
        addonManager.uninstallAddon(
            addon = addon,
            onSuccess = {
                refreshAddons { pendingInstallationTask = false }
                onSuccess?.invoke()
            },
            onError = { _, error ->
                pendingInstallationTask = false
                onError?.invoke(error)
            },
        )
    }

    private suspend fun refreshAddonsSus() {
        allAddons = addonManager.getAddons()
        val installedAddonsTemp = mutableListOf<Addon>()
        val recommendedAddonsTemp = mutableListOf<Addon>()
        val disabledAddonsTemp = mutableListOf<Addon>()
        val unsupportedAddonsTemp = mutableListOf<Addon>()
        allAddons.forEach { addon ->
            when {
                addon.inInstalledSection() -> installedAddonsTemp.add(addon)
                addon.inRecommendedSection() -> recommendedAddonsTemp.add(addon)
                addon.inDisabledSection() -> disabledAddonsTemp.add(addon)
                addon.inUnsupportedSection() -> unsupportedAddonsTemp.add(addon)
            }
        }
        installedAddons = installedAddonsTemp
        recommendedAddons = recommendedAddonsTemp
        disabledAddons = disabledAddonsTemp
        unsupportedAddons = unsupportedAddonsTemp
    }

    private fun refreshAddons(onComplete: (() -> Unit)? = null) {
        taskManager.processTask(
            type = AddonManagerTask.REFRESH,
            task = { refreshAddonsSus() },
            onComplete = {
                isBusy = !it
                onComplete?.invoke()
            },
        )
    }

    fun restartAddons() {
        taskManager.processTask(type = AddonManagerTask.RESET, task = {
            store.dispatch(ExtensionsProcessAction.EnabledAction)
            refreshAddonsSus()
        })
    }

    override fun start() {
        refreshAddons(onComplete = {initialLoad = false})
    }

    override fun stop() {
        scope.cancel()
    }

}