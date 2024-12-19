/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.shmibblez.inferno.compose.browserStore
import com.shmibblez.inferno.compose.sessionUseCases
import com.shmibblez.inferno.state.observeAsState
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState

// TODO:
//  -[ ] implement moz AwesomeBarFeature
//  -[ ] implement moz TabsToolbarFeature
//  -[ ] implement moz ReaderViewIntegration
//  -[ ] implement moz WebExtensionToolbarFeature
//  -[ ] implement moz engineView!!.setDynamicToolbarMaxHeight
//  -[ ] implement moz ToolbarIntegration

@Composable
fun BrowserToolbar(tabSessionState: TabSessionState) {
    val context = LocalContext.current
    val searchTerms = tabSessionState.content.searchTerms
    val url: String? by browserStore().observeAsState { state -> state.selectedTab?.content?.url }
    var editMode by remember { mutableStateOf(false) }
    val useCases = sessionUseCases()
    val siteSecure = tabSessionState.content.securityInfo.secure
    val trackingProtectionEnabled = tabSessionState.trackingProtection.enabled

    if (editMode) {
        BrowserEditToolbar(
            url = url ?: "<empty>",
            onUrlCommitted = { text ->
                useCases.loadUrl(text)
                editMode = false
            },
        )
    } else {
        BrowserDisplayToolbar(
            url = url ?: "<empty>",
            tabSessionState = tabSessionState
        )
    }
}

@Composable
fun BrowserDisplayToolbar(
    url: String,
    tabSessionState: TabSessionState
) {
    Row {
        ToolbarOrigin(tabSessionState)
    }
}

@Composable
fun BrowserEditToolbar(
    url: String,
    onUrlCommitted: (String) -> Unit = {},
) {
    var input by remember { mutableStateOf(url) }

    TextField(
        input,
        onValueChange = { value -> input = value },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(
            onGo = { onUrlCommitted(input) },
        ),
    )
}
