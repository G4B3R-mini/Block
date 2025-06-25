package com.shmibblez.inferno.bookmarks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.support.ktx.android.content.share

@Composable
fun InfernoBookmarksPage(
    goBack: () -> Unit,
    onNavToBrowser: () -> Unit,
    initialGuid: String = BookmarkRoot.Mobile.id,
) {

    val context = LocalContext.current
    val components = context.components
    val managerState by rememberBookmarksManagerState(initialGuid = initialGuid)

    InfernoSettingsPage(
        title = managerState.folder.title,
        goBack = goBack,
    ) {
        BookmarksManager(
            state = managerState,
            goBack = goBack,
            loadUrl = {
                onNavToBrowser.invoke()
                components.newTab(url = it, private = false)
            },
            copy = { url ->
                val urlClipData = ClipData.newPlainText(url, url)
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboardManager?.let {
                    it.setPrimaryClip(urlClipData)
                    Toast.makeText(context, R.string.url_copied, Toast.LENGTH_SHORT).show()
                }
            },
            share = { context.share(it) },
            openInNewTab = { url, private ->
                onNavToBrowser.invoke()
                components.newTab(url = url, private = private)
            },
            openAllInNewTab = { urls, private ->
                for (url in urls) {
                    components.newTab(url = url, private = private)
                }
            },
        )
    }
}