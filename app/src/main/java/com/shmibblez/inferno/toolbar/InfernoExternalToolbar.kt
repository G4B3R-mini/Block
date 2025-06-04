package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.shmibblez.inferno.browser.UiConst
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme
import mozilla.components.browser.state.state.TabSessionState

private val ICON_SIZE = 18.dp

@Composable
fun InfernoExternalToolbar(
    session: TabSessionState,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .background(
                LocalContext.current.infernoTheme().value.primaryBackgroundColor.copy(
                    alpha = UiConst.BAR_BG_ALPHA,
                )
            )
            .fillMaxWidth()
            .height(UiConst.EXTERNAL_TOOLBAR_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ICON_SIZE),
    ) {
        // back button
        InfernoIcon(
            painter = painterResource(R.drawable.ic_back_button),
            contentDescription = stringResource(R.string.browser_menu_tools),
            modifier = Modifier.size(18.dp),
        )

        // website title
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            InfernoText(text = session.content.title, infernoStyle = InfernoTextStyle.Normal)
            InfernoText(text = session.content.url, infernoStyle = InfernoTextStyle.Subtitle)
        }

        // todo: check what other options should be here
        // menu button
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = stringResource(R.string.browser_menu_back),
                modifier = Modifier.size(ICON_SIZE),
            )
            DropdownMenuItem(
                text = {
                    InfernoText(
                        text = stringResource(
                            R.string.browser_menu_open_in_fenix, stringResource(R.string.app_name)
                        ),
                    )
                },
                onClick =, // todo
            )
        }
    }
}