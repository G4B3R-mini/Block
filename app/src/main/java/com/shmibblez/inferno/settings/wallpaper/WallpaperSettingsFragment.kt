/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.wallpaper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import mozilla.components.lib.state.ext.observeAsComposableState
//import mozilla.telemetry.glean.private.NoExtras
import com.shmibblez.inferno.BrowserDirection
//import com.shmibblez.inferno.GleanMetrics.Wallpapers
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.compose.core.Action
import com.shmibblez.inferno.compose.snackbar.Snackbar
import com.shmibblez.inferno.compose.snackbar.SnackbarState
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.ext.showToolbar
import com.shmibblez.inferno.theme.FirefoxTheme
import com.shmibblez.inferno.wallpapers.Wallpaper

class WallpaperSettingsFragment : Fragment() {
    private val appStore by lazy {
        requireComponents.appStore
    }

    private val wallpaperUseCases by lazy {
        requireComponents.useCases.wallpaperUseCases
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
//        Wallpapers.wallpaperSettingsOpened.record(NoExtras())
        val wallpaperSettings = ComposeView(requireContext()).apply {
            setContent {
                FirefoxTheme {
                    val wallpapers = appStore.observeAsComposableState { state ->
                        state.wallpaperState.availableWallpapers
                    }.value ?: listOf()
                    val currentWallpaper = appStore.observeAsComposableState { state ->
                        state.wallpaperState.currentWallpaper
                    }.value ?: Wallpaper.Default

                    val coroutineScope = rememberCoroutineScope()

                    WallpaperSettings(
                        wallpaperGroups = wallpapers.groupByDisplayableCollection(),
                        defaultWallpaper = Wallpaper.Default,
                        selectedWallpaper = currentWallpaper,
                        loadWallpaperResource = {
                            wallpaperUseCases.loadThumbnail(it)
                        },
                        onSelectWallpaper = {
                            if (it.name != currentWallpaper.name) {
                                coroutineScope.launch {
                                    val result = wallpaperUseCases.selectWallpaper(it)
                                    onWallpaperSelected(it, result, requireView())
                                }
                            }
                        },
                        onLearnMoreClick = { url, collectionName ->
                            (activity as HomeActivity).openToBrowserAndLoad(
                                searchTermOrURL = url,
                                newTab = true,
                                from = BrowserDirection.FromWallpaper,
                            )
//                            Wallpapers.learnMoreLinkClick.record(
//                                Wallpapers.LearnMoreLinkClickExtra(
//                                    url = url,
//                                    collectionName = collectionName,
//                                ),
//                            )
                        },
                    )
                }
            }
        }

        // Using CoordinatorLayout as a parent view for the fragment gives the benefit of hiding
        // snackbars automatically when the fragment is closed.
        return CoordinatorLayout(requireContext()).apply {
            addView(wallpaperSettings)
        }
    }

    private fun onWallpaperSelected(
        wallpaper: Wallpaper,
        result: Wallpaper.ImageFileState,
        view: View,
    ) {
        when (result) {
            Wallpaper.ImageFileState.Downloaded -> {
                Snackbar.make(
                    snackBarParentView = view,
                    snackbarState = SnackbarState(
                        message = getString(R.string.wallpaper_updated_snackbar_message),
                        action = Action(
                            label = getString(R.string.wallpaper_updated_snackbar_action),
                            onClick = {
                                (activity as HomeActivity).browsingModeManager.mode = BrowsingMode.Normal
                                findNavController().navigate(R.id.homeFragment)
                            },
                        ),
                    ),
                ).show()

//                Wallpapers.wallpaperSelected.record(
//                    Wallpapers.WallpaperSelectedExtra(
//                        name = wallpaper.name,
//                        source = "settings",
//                        themeCollection = wallpaper.collection.name,
//                    ),
//                )
            }
            Wallpaper.ImageFileState.Error -> {
                Snackbar.make(
                    snackBarParentView = view,
                    snackbarState = SnackbarState(
                        message = getString(R.string.wallpaper_download_error_snackbar_message),
                        action = Action(
                            label = getString(R.string.wallpaper_download_error_snackbar_action),
                            onClick = {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    val retryResult = wallpaperUseCases.selectWallpaper(wallpaper)
                                    onWallpaperSelected(wallpaper, retryResult, view)
                                }
                            },
                        ),
                    ),
                ).show()
            }
            else -> { /* noop */ }
        }

        view.context.settings().showWallpaperOnboarding = false
    }

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.customize_wallpapers))
    }
}
