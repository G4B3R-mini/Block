package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.proto.InfernoSettings
import mozilla.components.browser.state.state.CustomTabSessionState

private val ICON_SIZE = 18.dp

// todo: not loading, reference ExternalAppBrowserFragment
// todo: setup custom tab in browser state once selected (reset customTabInitialized bool),
//  then copy logic below to initialize

@Composable
fun InfernoExternalToolbar(
    session: CustomTabSessionState?,
    onNavToBrowser: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onGoBack: (tabId: String) -> Unit,
    onGoForward: (tabId: String) -> Unit,
    onReload: (tabId: String) -> Unit,
    onShare: (url: String) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    // todo: reader mode not available for custom tabs?
//    val readerModeEnabled = session?.readerState?.readerable ?: false
    val isDesktopSite = session?.content?.desktopMode ?: false
    val canGoBack = session?.content?.canGoBack ?: false
    val canGoForward = session?.content?.canGoForward ?: false

    // todo: custom tab request desktop and nav actions, use icon since ToolbarOptions use normal tab session

    Row(
        modifier = Modifier
            .background(
                LocalContext.current.infernoTheme().value.primaryBackgroundColor.copy(
                    alpha = UiConst.BAR_BG_ALPHA,
                )
            )
            .fillMaxWidth()
            .height(UiConst.EXTERNAL_TOOLBAR_HEIGHT)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // back button
        InfernoIcon(
            painter = painterResource(R.drawable.ic_back_button_24),
            contentDescription = stringResource(R.string.browser_menu_tools),
            modifier = Modifier.size(18.dp),
        )

        // website title
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            InfernoText(
                text = session?.content?.title ?: "", infernoStyle = InfernoTextStyle.Normal,
                maxLines = 1,
            )
            InfernoText(
                text = session?.content?.url ?: "", infernoStyle = InfernoTextStyle.Subtitle,
                maxLines = 1,
            )
        }

        // menu button
        Box(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable { menuExpanded = !menuExpanded },
        ) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_menu_24),
                contentDescription = stringResource(R.string.browser_menu_back),
                modifier = Modifier.size(ICON_SIZE),
            )

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.padding(horizontal = 8.dp),
                containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
            ) {
                // powered by inferno browser
                InfernoText(
                    text = stringResource(
                        R.string.browser_menu_powered_by2, stringResource(R.string.app_name)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                // divider
                HorizontalDivider(
                    thickness = 1.dp,
                    color = LocalContext.current.infernoTheme().value.primaryIconColor,
                )

                // toggle desktop mode
                Row(
                    modifier = Modifier
                        .clickable {
                            onToggleDesktopMode.invoke()
                            menuExpanded = false
                        }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                ) {
                    InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_DESKTOP.ToToolbarIcon(
                        variant = isDesktopSite
                    )
                    InfernoText(
                        text = stringResource(
                            when (session?.content?.desktopMode ?: false) {
                                true -> R.string.browser_menu_switch_to_mobile_site
                                false -> R.string.browser_menu_switch_to_desktop_site
                            }
                        ),
                    )
                }

//                // enable reader mode
//                Row(
//                    modifier = Modifier.clickable(
//                        enabled = readerModeEnabled
//                    ) {
//                        onRequestReaderMode.invoke()
//                        menuExpanded = false
//                    },
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
//                ) {
//                    InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_READER_VIEW.ToToolbarIcon(
//                        tint = when (readerModeEnabled) {
//                            true -> LocalContext.current.infernoTheme().value.primaryIconColor
//                            false -> LocalContext.current.infernoTheme().value.secondaryIconColor
//                        },
//                    )
//                    InfernoText(
//                        text = stringResource(R.string.browser_menu_turn_on_reader_view),
//                        fontColor = when (readerModeEnabled) {
//                            true -> LocalContext.current.infernoTheme().value.primaryTextColor
//                            false -> LocalContext.current.infernoTheme().value.secondaryTextColor
//                        },
//                    )
//                }

                // open in browser
                Row(
                    modifier = Modifier
                        .clickable {
                            onNavToBrowser.invoke()
                            menuExpanded = false
                        }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                ) {
                    // inferno icon
                    Image(
                        painter = painterResource(R.drawable.inferno),
                        contentDescription = "",
                        modifier = Modifier.size(18.dp),
                    )
                    // open in browser text
                            InfernoText(
                                text = stringResource(
                                    R.string.browser_menu_open_in_fenix,
                                    stringResource(R.string.app_name)
                                ),
                            )
                }

                // divider
                HorizontalDivider(
                    thickness = 1.dp,
                    color = LocalContext.current.infernoTheme().value.primaryIconColor,
                )

                // nav options
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    ) {
                    // back
                    Box(
                        modifier = Modifier
                            .clickable(
                                enabled = canGoBack,
                                onClick = { session?.id?.let(onGoBack); menuExpanded = false },
                            ),
                    ) {
                        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK.ToToolbarIcon(
                            tint = when (canGoBack) {
                                true -> LocalContext.current.infernoTheme().value.primaryIconColor
                                false -> LocalContext.current.infernoTheme().value.secondaryIconColor
                            }
                        )
                    }
                    // forward
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable(
                                enabled = canGoForward,
                                onClick = { session?.id?.let(onGoForward); menuExpanded = false },
                            ),
                    ) {
                        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD.ToToolbarIcon(
                            tint = when (canGoForward) {
                                true -> LocalContext.current.infernoTheme().value.primaryIconColor
                                false -> LocalContext.current.infernoTheme().value.secondaryIconColor
                            }
                        )
                    }
                    // reload
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable(
                                onClick = { session?.id?.let(onReload); menuExpanded = false },
                            ),
                    ) {
                        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD.ToToolbarIcon()
                    }
                    // share
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable(
                                onClick = {
                                    session?.content?.url?.let(onShare); menuExpanded = false
                                },
                            ),
                    ) {
                        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHARE.ToToolbarIcon()
                    }
                }
            }
        }
    }
}