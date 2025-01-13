/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.onboarding

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import mozilla.components.lib.state.ext.observeAsComposableState
//import mozilla.telemetry.glean.private.NoExtras
//import com.shmibblez.inferno.GleanMetrics.Wallpapers
import com.shmibblez.inferno.NavGraphDirections
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.core.Action
import com.shmibblez.inferno.compose.snackbar.Snackbar
import com.shmibblez.inferno.compose.snackbar.SnackbarState
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.settings.wallpaper.getWallpapersForOnboarding
import com.shmibblez.inferno.theme.FirefoxTheme
import com.shmibblez.inferno.wallpapers.Wallpaper
import com.shmibblez.inferno.wallpapers.WallpaperOnboarding

/**
 * Dialog displaying the wallpapers onboarding.
 */
class WallpaperOnboardingDialogFragment : BottomSheetDialogFragment() {
    private val appStore by lazy {
        requireComponents.appStore
    }

    private val wallpaperUseCases by lazy {
        requireComponents.useCases.wallpaperUseCases
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                val bottomSheet = findViewById<View?>(R.id.design_bottom_sheet)
                BottomSheetBehavior.from(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.WallpaperOnboardingDialogStyle)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val currentWallpaper = requireContext().components.appStore.state.wallpaperState.currentWallpaper
        Wallpapers.onboardingClosed.record(
            Wallpapers.OnboardingClosedExtra(
                isSelected = currentWallpaper.name != Wallpaper.defaultName,
            ),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().settings().showWallpaperOnboarding = false
//        Wallpapers.onboardingOpened.record(NoExtras())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        this@WallpaperOnboardingDialogFragment.dialog?.setCanceledOnTouchOutside(true)

        setContent {
            FirefoxTheme {
                val wallpapers = appStore.observeAsComposableState { state ->
                    state.wallpaperState.availableWallpapers.getWallpapersForOnboarding()
                }.value ?: listOf()
                val currentWallpaper = appStore.observeAsComposableState { state ->
                    state.wallpaperState.currentWallpaper
                }.value ?: Wallpaper.Default

                val coroutineScope = rememberCoroutineScope()

                WallpaperOnboarding(
                    wallpapers = wallpapers,
                    currentWallpaper = currentWallpaper,
                    onCloseClicked = { dismiss() },
                    onExploreMoreButtonClicked = {
                        val directions = NavGraphDirections.actionGlobalWallpaperSettingsFragment()
                        findNavController().navigate(directions)
//                        Wallpapers.onboardingExploreMoreClick.record(NoExtras())
                    },
                    loadWallpaperResource = { wallpaperUseCases.loadThumbnail(it) },
                    onSelectWallpaper = {
                        coroutineScope.launch {
                            val result = wallpaperUseCases.selectWallpaper(it)
                            onWallpaperSelected(it, result, this@WallpaperOnboardingDialogFragment.requireView())
                        }
                    },
                )
            }
        }
    }

    private fun onWallpaperSelected(
        wallpaper: Wallpaper,
        result: Wallpaper.ImageFileState,
        view: View,
    ) {
        when (result) {
            Wallpaper.ImageFileState.Downloaded -> {
                Wallpapers.wallpaperSelected.record(
                    Wallpapers.WallpaperSelectedExtra(
                        name = wallpaper.name,
                        source = "onboarding",
                        themeCollection = wallpaper.collection.name,
                    ),
                )
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
    }

    companion object {
        // The number of wallpaper thumbnails to display.
        const val THUMBNAILS_SELECTION_COUNT = 6

        // The desired amount of seasonal wallpapers inside of the selector.
        const val SEASONAL_WALLPAPERS_COUNT = 3

        // The desired amount of seasonal wallpapers inside of the selector.
        const val CLASSIC_WALLPAPERS_COUNT = 2
    }
}
