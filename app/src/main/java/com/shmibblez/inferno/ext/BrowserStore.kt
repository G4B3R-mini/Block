package com.shmibblez.inferno.ext

import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore

fun BrowserStore.findExistingTabFromUrl(url: String): TabSessionState? {
    return state.tabs.firstOrNull {
        it.content.url == url
    }
}