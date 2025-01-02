/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.library.recentlyclosed

import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.state.recover.RecoverableTab
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.telemetry.glean.private.NoExtras
import com.shmibblez.inferno.BrowserDirection
import com.shmibblez.inferno.GleanMetrics.RecentlyClosedTabs
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.components.StoreProvider
import com.shmibblez.inferno.databinding.FragmentRecentlyClosedTabsBinding
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.setTextColor
import com.shmibblez.inferno.ext.showToolbar
import com.shmibblez.inferno.library.LibraryPageFragment

@Suppress("TooManyFunctions")
class RecentlyClosedFragment :
    LibraryPageFragment<RecoverableTab>(),
    UserInteractionHandler,
    MenuProvider {
    private lateinit var recentlyClosedFragmentStore: RecentlyClosedFragmentStore
    private var _recentlyClosedFragmentView: RecentlyClosedFragmentView? = null
    private val recentlyClosedFragmentView: RecentlyClosedFragmentView
        get() = _recentlyClosedFragmentView!!

    private lateinit var recentlyClosedInteractor: RecentlyClosedFragmentInteractor
    private lateinit var recentlyClosedController: RecentlyClosedController

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.library_recently_closed_tabs))
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        if (recentlyClosedFragmentStore.state.selectedTabs.isNotEmpty()) {
            inflater.inflate(R.menu.history_select_multi, menu)
            menu.findItem(R.id.delete_history_multi_select)?.let { deleteItem ->
                deleteItem.title = SpannableString(deleteItem.title)
                    .apply { setTextColor(requireContext(), R.attr.textCritical) }
            }
        } else {
            inflater.inflate(R.menu.library_menu, menu)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        val selectedTabs = recentlyClosedFragmentStore.state.selectedTabs

        return when (item.itemId) {
            R.id.close_history -> {
                close()
                RecentlyClosedTabs.menuClose.record(NoExtras())
                true
            }
            R.id.share_history_multi_select -> {
                recentlyClosedController.handleShare(selectedTabs)
                true
            }
            R.id.delete_history_multi_select -> {
                recentlyClosedController.handleDelete(selectedTabs)
                true
            }
            R.id.open_history_in_new_tabs_multi_select -> {
                recentlyClosedController.handleOpen(selectedTabs, BrowsingMode.Normal)
                true
            }
            R.id.open_history_in_private_tabs_multi_select -> {
                recentlyClosedController.handleOpen(selectedTabs, BrowsingMode.Private)
                true
            }
            // other options are not handled by this menu provider
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RecentlyClosedTabs.opened.record(NoExtras())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentRecentlyClosedTabsBinding.inflate(inflater, container, false)
        recentlyClosedFragmentStore = StoreProvider.get(this) {
            RecentlyClosedFragmentStore(
                RecentlyClosedFragmentState(
                    items = listOf(),
                    selectedTabs = emptySet(),
                ),
            )
        }
        recentlyClosedController = DefaultRecentlyClosedController(
            navController = findNavController(),
            browserStore = requireComponents.core.store,
            recentlyClosedStore = recentlyClosedFragmentStore,
            activity = activity as HomeActivity,
            tabsUseCases = requireComponents.useCases.tabsUseCases,
            recentlyClosedTabsStorage = requireComponents.core.recentlyClosedTabsStorage.value,
            lifecycleScope = lifecycleScope,
            openToBrowser = ::openItem,
        )
        recentlyClosedInteractor = RecentlyClosedFragmentInteractor(recentlyClosedController)
        _recentlyClosedFragmentView = RecentlyClosedFragmentView(
            binding.recentlyClosedLayout,
            recentlyClosedInteractor,
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _recentlyClosedFragmentView = null
    }

    private fun openItem(url: String, mode: BrowsingMode? = null) {
        mode?.let { (activity as HomeActivity).browsingModeManager.mode = it }

        (activity as HomeActivity).openToBrowserAndLoad(
            searchTermOrURL = url,
            newTab = true,
            from = BrowserDirection.FromRecentlyClosed,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        consumeFrom(recentlyClosedFragmentStore) { state ->
            recentlyClosedFragmentView.update(state)
            activity?.invalidateOptionsMenu()
        }

        requireComponents.core.store.flowScoped(viewLifecycleOwner) { flow ->
            flow.map { state -> state.closedTabs }
                .distinctUntilChanged()
                .collect { tabs ->
                    recentlyClosedFragmentStore.dispatch(
                        RecentlyClosedFragmentAction.Change(tabs),
                    )
                }
        }
    }

    override val selectedItems: Set<RecoverableTab> = setOf()

    override fun onBackPressed(): Boolean {
        return recentlyClosedController.handleBackPressed()
    }
}
