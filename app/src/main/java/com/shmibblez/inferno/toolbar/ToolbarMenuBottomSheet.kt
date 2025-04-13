package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.DividerToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.FindInPageToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.NavOptionsToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.PrivateModeToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.ReaderViewToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.RequestDesktopSiteToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.SettingsToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.ShareToolbarMenuItem
import mozilla.components.browser.state.state.TabSessionState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarMenuBottomSheet(
    tabSessionState: TabSessionState?,
    loading: Boolean,
    onDismissMenuBottomSheet: () -> Unit,
    onActivateFindInPage: () -> Unit,
    onActivateReaderView: () -> Unit,
    onNavToSettings: () -> Unit,
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
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            // todo: move to bottom sticky when switch to grid view
            // stop, refresh, forward, back
            NavOptionsToolbarMenuItem(loading)

            DividerToolbarMenuItem()

            ShareToolbarMenuItem()
            DividerToolbarMenuItem()

            PrivateModeToolbarMenuItem(
                isPrivateMode = tabSessionState.content.private,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
            DividerToolbarMenuItem()

            RequestDesktopSiteToolbarMenuItem(desktopMode = tabSessionState.content.desktopMode)
            DividerToolbarMenuItem()

            FindInPageToolbarMenuItem(
                onActivateFindInPage = onActivateFindInPage,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
            DividerToolbarMenuItem()

            ReaderViewToolbarMenuItem(
                enabled = tabSessionState.readerState.readerable,
                onActivateReaderView = onActivateReaderView,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
            DividerToolbarMenuItem()

            SettingsToolbarMenuItem(onNavToSettings = onNavToSettings)

            // fill remaining space
            Spacer(modifier = Modifier.weight(1F))
        }
    }
}