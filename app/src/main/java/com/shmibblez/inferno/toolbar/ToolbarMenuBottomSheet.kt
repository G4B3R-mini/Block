package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarFindInPage
import mozilla.components.browser.state.state.TabSessionState
import com.shmibblez.inferno.toolbar.MenuOnlyComponents.Companion.NavOptions
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarPrivateModeToggle
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarRequestReaderView
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarBack
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarForward
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarOriginMini
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarReload
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarRequestDesktopSite
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarSettings
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarShare
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarShowTabsTray


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarMenuBottomSheet(
    tabSessionState: TabSessionState?,
    loading: Boolean,
    tabCount: Int,
    onDismissMenuBottomSheet: () -> Unit,
    onActivateFindInPage: () -> Unit,
    onActivateReaderView: () -> Unit,
    onRequestSearchBar: () -> Unit,
    onNavToSettings: () -> Unit,
    onNavToTabsTray: () -> Unit,
) {
    if (tabSessionState == null) return
    ModalBottomSheet(
        onDismissRequest = onDismissMenuBottomSheet,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Black.copy(alpha = 0.75F),
        scrimColor = Color.Black.copy(alpha = 0.5F),
        shape = RectangleShape,
        dragHandle = { /* no drag handle */
            // in case want to add one, make custom component centered in middle
//            BottomSheetDefaults.DragHandle(
//                color = Color.White,
//                height = SHEET_HANDLE_HEIGHT,
////            shape = RectangleShape,
//            )
        },
    ) {
        // todo: make list stored in prefs for custom order, remaining items in bottom / top with
        //  red marker top right for new (if not in prefs list), popup menu for move to bottom
        //  or move to top
        val items: List<@Composable LazyGridItemScope.() -> Unit> = listOf(
            { ToolbarShare(type = ToolbarOptionType.EXPANDED) },
            {
                ToolbarPrivateModeToggle(
                    type = ToolbarOptionType.EXPANDED,
                    isPrivateMode = tabSessionState.content.private,
                    dismissMenuSheet = onDismissMenuBottomSheet,
                )
            },
            {
                ToolbarRequestDesktopSite(
                    type = ToolbarOptionType.EXPANDED,
                    desktopMode = tabSessionState.content.desktopMode,
                    dismissMenuSheet = onDismissMenuBottomSheet,
                )
            },
            {
                ToolbarFindInPage(
                    type = ToolbarOptionType.EXPANDED,
                    onActivateFindInPage = onActivateFindInPage,
                    dismissMenuSheet = onDismissMenuBottomSheet,
                )
            },
            {
                ToolbarRequestReaderView(
                    type = ToolbarOptionType.EXPANDED,
                    enabled = tabSessionState.readerState.readerable,
                    dismissMenuSheet = onDismissMenuBottomSheet,
                    onActivateReaderView = onActivateReaderView,
                )
            },
            {
                ToolbarShowTabsTray(
                    type = ToolbarOptionType.EXPANDED,
                    tabCount = tabCount,
                    dismissMenuSheet = onDismissMenuBottomSheet,
                    onNavToTabsTray =   onNavToTabsTray,

                )
            },
            {
                ToolbarBack(
                    type = ToolbarOptionType.EXPANDED,
                    enabled = tabSessionState.content.canGoBack,
                )
            },
            {
                ToolbarForward(
                    type = ToolbarOptionType.EXPANDED,
                    enabled = tabSessionState.content.canGoForward,
                )
            },
            {
                ToolbarReload(
                    type = ToolbarOptionType.EXPANDED,
                    enabled = true,
                    loading = loading,
                    dismissMenuSheet = onDismissMenuBottomSheet,
                )
            },
            {
                ToolbarOriginMini(
                    type = ToolbarOptionType.EXPANDED,
                    onRequestSearchBar = onRequestSearchBar,
                )
            },
            {
                ToolbarSettings(
                    type = ToolbarOptionType.EXPANDED,
                    onNavToSettings = onNavToSettings,
                )
            },
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
//            modifier = Modifier.padding(horizontal = 16.dp), doable?
            contentPadding = PaddingValues(start = 16.dp, bottom = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(
                24.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalArrangement = Arrangement.spacedBy(
                8.dp,
                alignment = Alignment.CenterHorizontally
            ),
        ) {
            navHeader {
                // todo: move to bottom sticky when switch to grid view
                // stop, refresh, forward, back
                NavOptions(loading)
            }

            items(items) { option ->
                option.invoke(this)
            }
        }
    }
}

private fun LazyGridScope.navHeader(content: @Composable LazyGridItemScope.() -> Unit) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}