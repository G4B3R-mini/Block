/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.tabstray.browser

import android.view.View
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import mozilla.components.lib.state.ext.observeAsComposableState
import mozilla.telemetry.glean.private.NoExtras
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.components
import com.shmibblez.inferno.compose.ComposeViewHolder
import com.shmibblez.inferno.compose.snackbar.Snackbar
import com.shmibblez.inferno.compose.snackbar.SnackbarState
import com.shmibblez.inferno.tabstray.TabsTrayFragment.Companion.ELEVATION
import com.shmibblez.inferno.tabstray.TabsTrayState
import com.shmibblez.inferno.tabstray.TabsTrayStore
import com.shmibblez.inferno.tabstray.TrayPagerAdapter
import com.shmibblez.inferno.tabstray.inactivetabs.InactiveTabsList
import com.shmibblez.inferno.GleanMetrics.TabsTray as TabsTrayMetrics

/**
 * The [ComposeViewHolder] for displaying the section of inactive tabs in [TrayPagerAdapter].
 *
 * @param composeView [ComposeView] which will be populated with Jetpack Compose UI content.
 * @param lifecycleOwner [LifecycleOwner] to which this Composable will be tied to.
 * @param tabsTrayStore [TabsTrayStore] used to listen for changes to [TabsTrayState.inactiveTabs].
 * @param interactor [InactiveTabsInteractor] used to respond to interactions with the inactive tabs header
 * and the auto close dialog.
 */
class InactiveTabViewHolder(
    composeView: ComposeView,
    lifecycleOwner: LifecycleOwner,
    private val tabsTrayStore: TabsTrayStore,
    private val interactor: InactiveTabsInteractor,
) : ComposeViewHolder(composeView, lifecycleOwner) {

    @Composable
    override fun Content() {
        val expanded = components.appStore
            .observeAsComposableState { state -> state.inactiveTabsExpanded }.value ?: false
        val inactiveTabs = tabsTrayStore
            .observeAsComposableState { state -> state.inactiveTabs }.value ?: emptyList()
        val showInactiveTabsAutoCloseDialog =
            components.settings.shouldShowInactiveTabsAutoCloseDialog(inactiveTabs.size)
        var showAutoClosePrompt by remember { mutableStateOf(showInactiveTabsAutoCloseDialog) }

        if (showInactiveTabsAutoCloseDialog) {
            TabsTrayMetrics.autoCloseSeen.record(NoExtras())
        }

        if (inactiveTabs.isNotEmpty()) {
            InactiveTabsList(
                inactiveTabs = inactiveTabs,
                expanded = expanded,
                showAutoCloseDialog = showAutoClosePrompt,
                showCFR = false, // The CFR in XML is handled by [TabsTrayInactiveTabsOnboardingBinding]
                onHeaderClick = { interactor.onInactiveTabsHeaderClicked(!expanded) },
                onDeleteAllButtonClick = interactor::onDeleteAllInactiveTabsClicked,
                onAutoCloseDismissClick = {
                    interactor.onAutoCloseDialogCloseButtonClicked()
                    showAutoClosePrompt = !showAutoClosePrompt
                },
                onEnableAutoCloseClick = {
                    interactor.onEnableAutoCloseClicked()
                    showAutoClosePrompt = !showAutoClosePrompt
                    showConfirmationSnackbar()
                },
                onTabClick = interactor::onInactiveTabClicked,
                onTabCloseClick = interactor::onInactiveTabClosed,
                onCFRShown = {},
                onCFRClick = {},
                onCFRDismiss = {},
            )
        }
    }

    override val allowPrivateTheme: Boolean
        get() = false

    private fun showConfirmationSnackbar() {
        Snackbar.make(
            snackBarParentView = composeView.rootView,
            snackbarState = SnackbarState(
                message = composeView.context.getString(R.string.inactive_tabs_auto_close_message_snackbar),
                duration = SnackbarDuration.Long,
            ),
        ).apply {
            view.elevation = ELEVATION
            show()
        }
    }

    companion object {
        val LAYOUT_ID = View.generateViewId()
    }
}
