package com.shmibblez.inferno.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.UiConst
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.library.history.History

private val ICON_SIZE = 18.dp

@Composable
fun InfernoHistoryPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val historyViewerState by rememberHistoryViewerState()
    var showConfirmDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showDeleteTimeRangeDialog by remember { mutableStateOf(false) }

    LaunchedEffect((historyViewerState.mode as? HistoryViewerState.Mode.Selection)?.selectedItems) {
        val selectedItems =
            (historyViewerState.mode as? HistoryViewerState.Mode.Selection)?.selectedItems
        if (selectedItems?.isEmpty() == true) {
            historyViewerState.switchToNormalMode()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            when (historyViewerState.mode) {
                is HistoryViewerState.Mode.Selection -> EditingTopBar(
                    onStopEditing = { historyViewerState.switchToNormalMode() },
                    mode = historyViewerState.mode as HistoryViewerState.Mode.Selection,
                    onShareSelected = { historyViewerState.shareSelected() },
                    onOpenSelectedInBrowser = {
                        historyViewerState.openSelectedInBrowser(
                            private = false, then = goBack
                        )
                    },
                    onOpenSelectedInBrowserPrivate = {
                        historyViewerState.openSelectedInBrowser(
                            private = true, then = goBack
                        )
                    },
                    onDeleteSelected = { showConfirmDeleteSelectedDialog = true },
                )

                HistoryViewerState.Mode.Syncing,
                HistoryViewerState.Mode.Normal,
                    -> NormalTopBar(
                    goBack = goBack,
                    onDeleteSelected = { showDeleteTimeRangeDialog = true },
                )
            }
        },
        containerColor = context.infernoTheme().value.primaryBackgroundColor,
    ) { edgeInsets ->
        HistoryViewer(
            state = historyViewerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(edgeInsets),
            onOpenHistoryItem = {
                when (it) {
                    is History.Group -> {}
                    is History.Metadata -> {
                        context.components.newTab(url = it.url)
                    }

                    is History.Regular -> {
                        // used to be loadUrl but replaces tab
                        // context.components.useCases.sessionUseCases.loadUrl(url = it.url)
                        context.components.newTab(url = it.url)
                    }
                }
                goBack.invoke()
            },
        )

        // show delete selected dialog
        if (showConfirmDeleteSelectedDialog) {
            ConfirmDeleteSelectedDialog(
                onDismiss = { showConfirmDeleteSelectedDialog = false },
                onConfirm = {
                    historyViewerState.deleteSelected()
                    showConfirmDeleteSelectedDialog = false
                },
            )
        } else if (showDeleteTimeRangeDialog) {
            // todo: time range selector dialog, use radio buttons (copy firefox ui)

        }
    }
}

// todo: actions
//  - search
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopBar(
    goBack: () -> Unit,
    onDeleteSelected: () -> Unit,
) {
    TopAppBar(
        title = {
            InfernoText(
                text = stringResource(R.string.library_history),
                infernoStyle = InfernoTextStyle.Title,
                modifier = Modifier.padding(horizontal = UiConst.TOP_BAR_INTERNAL_PADDING)
            )
        },
        colors = TopAppBarColors(
            containerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
            scrolledContainerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
            navigationIconContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            titleContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            actionIconContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        ),
        navigationIcon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_back_button_24),
                contentDescription = "",
                modifier = Modifier
                    .padding(start = UiConst.TOP_BAR_INTERNAL_PADDING)
                    .size(ICON_SIZE)
                    .clickable(onClick = goBack),
            )
        },
        actions = {
            // todo: when in search mode, replace title with BasicEditText with no border
//        InfernoIcon(
//            painter = painterResource(R.drawable.ic_search_24),
//            contentDescription = "",
//            modifier = Modifier
//                .size(ICON_SIZE)
//                .clickable { showConfirmDeleteAllDialog = true },
//        )
            InfernoIcon(
                painter = painterResource(R.drawable.ic_delete_24),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                    .size(ICON_SIZE)
                    .clickable(onClick = onDeleteSelected),
            )
        },
    )
}

// todo: actions (3 dot menu with options)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditingTopBar(
    onStopEditing: () -> Unit,
    mode: HistoryViewerState.Mode.Selection,
    onShareSelected: () -> Unit,
    onOpenSelectedInBrowser: () -> Unit,
    onOpenSelectedInBrowserPrivate: () -> Unit,
    onDeleteSelected: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            InfernoText(
                text = stringResource(
                    R.string.history_multi_select_title,
                    mode.selectedItems.size,
                ),
                infernoStyle = InfernoTextStyle.Title,
                modifier = Modifier.padding(horizontal = UiConst.TOP_BAR_INTERNAL_PADDING),
            )
        },
        colors = TopAppBarColors(
            containerColor = LocalContext.current.infernoTheme().value.primaryActionColor,
            scrolledContainerColor = LocalContext.current.infernoTheme().value.primaryActionColor,
            navigationIconContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            titleContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            actionIconContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        ),
        navigationIcon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_close_24),
                contentDescription = "",
                modifier = Modifier
                    .padding(start = UiConst.TOP_BAR_INTERNAL_PADDING)
                    .size(ICON_SIZE)
                    .clickable(onClick = onStopEditing),
            )
        },
        actions = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_share_24),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                    .size(ICON_SIZE)
                    .clickable(onClick = onShareSelected),
            )
            Box(
                modifier = Modifier
                    .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                    .size(ICON_SIZE)
                    .clickable { menuExpanded = !menuExpanded },
            ) {
                // menu icon
                InfernoIcon(
                    painter = painterResource(R.drawable.ic_menu_24),
                    contentDescription = "",
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                ) {
                    // open in browser normal
                    DropdownMenuItem(
                        text = {
                            InfernoText(
                                text = stringResource(R.string.bookmark_menu_open_in_new_tab_button),
                                infernoStyle = InfernoTextStyle.Normal,
                            )
                        },
                        onClick = {
                            onOpenSelectedInBrowser.invoke()
                            menuExpanded = false
                        },
                    )
                    // open in browser private
                    DropdownMenuItem(
                        text = {
                            InfernoText(
                                text = stringResource(R.string.bookmark_menu_open_in_private_tab_button),
                                infernoStyle = InfernoTextStyle.Normal,
                            )
                        },
                        onClick = {
                            onOpenSelectedInBrowserPrivate.invoke()
                            menuExpanded = false
                        },
                    )
                    // on delete
                    DropdownMenuItem(
                        text = {
                            InfernoText(
                                text = stringResource(R.string.bookmark_menu_delete_button),
                                infernoStyle = InfernoTextStyle.Normal,
                                fontColor = LocalContext.current.infernoTheme().value.errorColor,
                            )
                        },
                        onClick = {
                            onDeleteSelected.invoke()
                            menuExpanded = false
                        },
                    )
                }
            }
        },
    )
}