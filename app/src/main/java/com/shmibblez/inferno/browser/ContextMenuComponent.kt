package com.shmibblez.inferno.browser

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Space
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentManager
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.findTabOrCustomTab
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.browser.state.state.content.ShareInternetResourceState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.engine.HitResult
import mozilla.components.feature.app.links.AppLinksUseCases
import mozilla.components.feature.contextmenu.ContextMenuCandidate
import mozilla.components.feature.contextmenu.ContextMenuCandidate.Companion.MAX_TITLE_LENGTH
import mozilla.components.feature.contextmenu.ContextMenuFeature
import mozilla.components.feature.contextmenu.ContextMenuUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.log.Log
import mozilla.components.support.ktx.android.content.addContact
import mozilla.components.support.ktx.android.content.createChooserExcludingCurrentApp
import mozilla.components.support.ktx.android.content.share
import mozilla.components.support.ktx.kotlin.stripMailToProtocol
import mozilla.components.support.ktx.kotlin.takeOrReplace

/**
 * replaces [ContextMenuFeature]
 *
 * Component for displaying a context menu after long-pressing web content.
 *
 * This feature will subscribe to the currently selected tab and display a context menu based on
 * the [HitResult] in its `ContentState`. Once the context menu is closed or the user selects an
 * item from the context menu the related [HitResult] will be consumed.
 *
 * @param store The [BrowserStore] this feature should subscribe to.
 * @param engineView The [EngineView]] this feature component should show context menus for.
 * @param tabId Optional id of a tab. Instead of showing context menus for the currently selected tab this feature will
 * show only context menus for this tab if an id is provided.
 * @param additionalNote which it will be attached to the bottom of context menu but for a specific [HitResult]
 */

@Composable
fun ContextMenuComponent(
    store: BrowserStore,
    engineView: EngineView?,
    useCases: ContextMenuUseCases,
    tabId: String? = null,
    additionalNote: (HitResult) -> String? = { null },
) {
    val context = LocalContext.current
    val candidates: List<ContextMenuCandidate> = remember {
        generateDefaultCandidates(
            context = context,
            tabsUseCases = context.components.useCases.tabsUseCases,
            contextMenuUseCases = context.components.useCases.contextMenuUseCases,
        )
    }
    var scope by remember { mutableStateOf<CoroutineScope?>(null) }
    var hitResult by remember { mutableStateOf<HitResult?>(null) }
    var state by remember { mutableStateOf<SessionState?>(null) }

    fun dismissDialog() {
        if (state != null) useCases.consumeHitResult(state!!.id)
        state = null
        hitResult = null
    }

    fun onMenuItemSelected(tabId: String, itemId: String) {
        val tab = store.state.findTabOrCustomTab(tabId) ?: return
        val candidate = candidates.find { it.id == itemId } ?: return

        useCases.consumeHitResult(tab.id)

        tab.content.hitResult?.let { hitResult ->
            candidate.action.invoke(tab, hitResult)
//            emitClickFact(candidate)
        }

        dismissDialog()
    }

    DisposableEffect(null) {
        scope = store.flowScoped { flow ->
            flow.map { state -> state.findTabOrCustomTabOrSelectedTab(tabId) }
                .distinctUntilChangedBy { it?.content?.hitResult }.collect { newState ->
                    val newHitResult = newState?.content?.hitResult
                    if (newHitResult != null) {
                        hitResult = newHitResult // replaces showContextMenu(state, newHitResult)
                        state = newState
                    } else {
                        dismissDialog() // replaces hideContextMenu()
                    }
                }
        }
        onDispose {
            scope?.cancel()
        }
    }

    if (state != null && hitResult != null) {
        val tab = state!!
        val (ids, labels) = candidates.filter { candidate -> candidate.showFor(tab, hitResult!!) }
            .fold(Pair(mutableListOf<String>(), mutableListOf<String>())) { items, candidate ->
                items.first.add(candidate.id)
                items.second.add(candidate.label)
                items
            }

        // We have no context menu items to show for this HitResult. Let's consume it to remove it from the Session.
        if (ids.isEmpty()) {
            useCases.consumeHitResult(tab.id)
            dismissDialog()
            return
        }

        // We know that we are going to show a context menu. Now is the time to perform the haptic feedback.
        engineView?.asView()?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

        val note = additionalNote(hitResult!!)

        // show dialog
        Dialog(
            onDismissRequest = ::dismissDialog,
//            properties = DialogProperties(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(Color(0xFF141414)),
            ) {
                item {
                    // todo: nest in column and make this stay at the top all the time, or make sticky
                    ContextMenuTitle(title = hitResult!!.getLink())
                }

                itemsIndexed(labels) { i, label ->
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.Red,
                    )
                    ContextMenuItem(
                        label = label,
                        onClick = {
                            onMenuItemSelected(state!!.id, ids[i])
                            dismissDialog()
                        },
                    )
                }

                item {
                    if (!note.isNullOrBlank()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.Red,
                        )
                        AdditionalNote(
                            additionalNote = note,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuTitle(title: String) {
    InfernoText(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        fontColor = Color.White,
    )
}

@Composable
private fun ContextMenuItem(label: String, onClick: () -> Unit) {
    InfernoText(
        text = label,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        fontColor = Color.White,
    )
}

@Composable
private fun AdditionalNote(additionalNote: String?) {
    if (additionalNote == null) return
    InfernoText(
        text = additionalNote,
        modifier = Modifier.padding(24.dp),
        fontSize = 12.sp,
        fontColor = Color.White,
    )
}

private fun generateDefaultCandidates(
    context: Context,
    tabsUseCases: TabsUseCases,
    contextMenuUseCases: ContextMenuUseCases,
): List<ContextMenuCandidate> {
    val contextMenuCandidateAppLinksUseCases = AppLinksUseCases(
        context,
        { true },
    )
    val openInExternalAppCandidate = ContextMenuCandidate.createOpenInExternalAppCandidate(
        context,
        contextMenuCandidateAppLinksUseCases,
    )

    val candidates: List<ContextMenuCandidate> = listOf(
        openInExternalAppCandidate,
        createOpenInNewTabCandidate(context, tabsUseCases),
        createOpenInPrivateTabCandidate(context, tabsUseCases),
        createCopyLinkCandidate(context),
        createDownloadLinkCandidate(context, contextMenuUseCases),
        createShareLinkCandidate(context),
        createShareImageCandidate(context, contextMenuUseCases),
        createOpenImageInNewTabCandidate(context, tabsUseCases),
        createCopyImageCandidate(context, contextMenuUseCases),
        createSaveImageCandidate(context, contextMenuUseCases),
        createSaveVideoAudioCandidate(context, contextMenuUseCases),
        createCopyImageLocationCandidate(context),
        createAddContactCandidate(context),
        createShareEmailAddressCandidate(context),
        createCopyEmailAddressCandidate(context),
    )


    return candidates
}

/**
 * Context Menu item: "Open Link in New Tab".
 *
 * @param context [Context] used for various system interactions.
 * @param tabsUseCases [TabsUseCases] used for adding new tabs.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createOpenInNewTabCandidate(
    context: Context,
    tabsUseCases: TabsUseCases,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.open_in_new_tab",
    label = context.getString(R.string.mozac_feature_contextmenu_open_link_in_new_tab),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isHttpLink() && !tab.content.private && additionalValidation(
            tab, hitResult
        )
    },
    action = { parent, hitResult ->
        val tab = tabsUseCases.addTab(
            hitResult.getLink(),
            selectTab = false,
            startLoading = true,
            parentId = parent.id,
            contextId = parent.contextId,
        )

        // todo: show snackbar
//        snackbarDelegate.show(
//            snackBarParentView = snackBarParentView,
//            text = R.string.mozac_feature_contextmenu_snackbar_new_tab_opened,
//            duration = Snackbar.LENGTH_LONG,
//            action = R.string.mozac_feature_contextmenu_snackbar_action_switch,
//        ) {
//            tabsUseCases.selectTab(tab)
//        }
    },
)

/**
 * Context Menu item: "Open Link in Private Tab".
 *
 * @param context [Context] used for various system interactions.
 * @param tabsUseCases [TabsUseCases] used for adding new tabs.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.         */
fun createOpenInPrivateTabCandidate(
    context: Context,
    tabsUseCases: TabsUseCases,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.open_in_private_tab",
    label = context.getString(R.string.mozac_feature_contextmenu_open_link_in_private_tab),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isHttpLink() && additionalValidation(
            tab, hitResult
        )
    },
    action = { parent, hitResult ->
        val tab = tabsUseCases.addTab(
            hitResult.getLink(),
            selectTab = false,
            startLoading = true,
            parentId = parent.id,
            private = true,
        )

        // todo: show snackbar
//        snackbarDelegate.show(
//            snackBarParentView = snackBarParentView,
//            text = R.string.mozac_feature_contextmenu_snackbar_new_private_tab_opened,
//            duration = Snackbar.LENGTH_LONG,
//            action = R.string.mozac_feature_contextmenu_snackbar_action_switch,
//        ) {
//            tabsUseCases.selectTab(tab)
//        }
    },
)

/**
 * Context Menu item: "Open Link in external App".
 *
 * @param context [Context] used for various system interactions.
 * @param appLinksUseCases [AppLinksUseCases] used to interact with urls that can be opened in 3rd party apps.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createOpenInExternalAppCandidate(
    context: Context,
    appLinksUseCases: AppLinksUseCases,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.open_in_external_app",
    label = context.getString(R.string.mozac_feature_contextmenu_open_link_in_external_app),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.canOpenInExternalApp(
            appLinksUseCases
        ) && additionalValidation(tab, hitResult)
    },
    action = { _, hitResult ->
        val link = hitResult.getLink()
        val redirect = appLinksUseCases.appLinkRedirectIncludeInstall(link)
        val appIntent = redirect.appIntent
        val marketPlaceIntent = redirect.marketplaceIntent
        if (appIntent != null) {
            appLinksUseCases.openAppLink(appIntent)
        } else if (marketPlaceIntent != null) {
            appLinksUseCases.openAppLink(marketPlaceIntent)
        }
    },
)

/**
 * Context Menu item: "Add to contact".
 *
 * @param context [Context] used for various system interactions.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createAddContactCandidate(
    context: Context,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.add_to_contact",
    label = context.getString(R.string.mozac_feature_contextmenu_add_to_contact),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isMailto() && additionalValidation(
            tab, hitResult
        )
    },
    action = { _, hitResult -> context.addContact(hitResult.getLink().stripMailToProtocol()) },
)

/**
 * Context Menu item: "Share email address".
 *
 * @param context [Context] used for various system interactions.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createShareEmailAddressCandidate(
    context: Context,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.share_email",
    label = context.getString(R.string.mozac_feature_contextmenu_share_email_address),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isMailto() && additionalValidation(
            tab, hitResult
        )
    },
    action = { _, hitResult -> context.share(hitResult.getLink().stripMailToProtocol()) },
)

/**
 * Context Menu item: "Copy email address".
 *
 * @param context [Context] used for various system interactions.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createCopyEmailAddressCandidate(
    context: Context,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.copy_email_address",
    label = context.getString(R.string.mozac_feature_contextmenu_copy_email_address),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isMailto() && additionalValidation(
            tab, hitResult
        )
    },
    action = { _, hitResult ->
        val email = hitResult.getLink().stripMailToProtocol()
        clipPlainText(
            context,
            email,
            email,
            R.string.mozac_feature_contextmenu_snackbar_email_address_copied,
        )
    },
)

/**
 * Context Menu item: "Open Image in New Tab".
 *
 * @param context [Context] used for various system interactions.
 * @param tabsUseCases [TabsUseCases] used for adding new tabs.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createOpenImageInNewTabCandidate(
    context: Context,
    tabsUseCases: TabsUseCases,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.open_image_in_new_tab",
    label = context.getString(R.string.mozac_feature_contextmenu_open_image_in_new_tab),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isImage() && additionalValidation(
            tab, hitResult
        )
    },
    action = { parent, hitResult ->
        val tab = tabsUseCases.addTab(
            hitResult.src,
            selectTab = false,
            startLoading = true,
            parentId = parent.id,
            contextId = parent.contextId,
            private = parent.content.private,
        )

        // todo: show snackbar
//        snackbarDelegate.show(
//            snackBarParentView = snackBarParentView,
//            text = R.string.mozac_feature_contextmenu_snackbar_new_tab_opened,
//            duration = Snackbar.LENGTH_LONG,
//            action = R.string.mozac_feature_contextmenu_snackbar_action_switch,
//        ) {
//            tabsUseCases.selectTab(tab)
//        }
    },
)

/**
 * Context Menu item: "Save image".
 *
 * @param context [Context] used for various system interactions.
 * @param contextMenuUseCases [ContextMenuUseCases] used to integrate other features.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createSaveImageCandidate(
    context: Context,
    contextMenuUseCases: ContextMenuUseCases,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.save_image",
    label = context.getString(R.string.mozac_feature_contextmenu_save_image),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isImage() && additionalValidation(
            tab, hitResult
        )
    },
    action = { tab, hitResult ->
        contextMenuUseCases.injectDownload(
            tab.id,
            DownloadState(
                hitResult.src,
                skipConfirmation = true,
                private = tab.content.private,
                referrerUrl = tab.content.url,
            ),
        )
    },
)

/**
 * Context Menu item: "Copy image".
 *
 * @param context [Context] used for various system interactions.
 * @param contextMenuUseCases [ContextMenuUseCases] used to integrate other features.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createCopyImageCandidate(
    context: Context,
    contextMenuUseCases: ContextMenuUseCases,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.copy_image",
    label = context.getString(R.string.mozac_feature_contextmenu_copy_image),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isImage() && additionalValidation(
            tab, hitResult
        )
    },
    action = { tab, hitResult ->
        contextMenuUseCases.injectCopyFromInternet(
            tab.id,
            ShareInternetResourceState(
                url = hitResult.src,
                private = tab.content.private,
                referrerUrl = tab.content.url,
            ),
        )
    },
)

/**
 * Context Menu item: "Save video".
 *
 * @param context [Context] used for various system interactions.
 * @param contextMenuUseCases [ContextMenuUseCases] used to integrate other features.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createSaveVideoAudioCandidate(
    context: Context,
    contextMenuUseCases: ContextMenuUseCases,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.save_video",
    label = context.getString(R.string.mozac_feature_contextmenu_save_file_to_device),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isVideoAudio() && additionalValidation(
            tab, hitResult
        )
    },
    action = { tab, hitResult ->
        contextMenuUseCases.injectDownload(
            tab.id,
            DownloadState(
                hitResult.src,
                skipConfirmation = true,
                private = tab.content.private,
                referrerUrl = tab.content.url,
            ),
        )
    },
)

/**
 * Context Menu item: "Save link".
 *
 * @param context [Context] used for various system interactions.
 * @param contextMenuUseCases [ContextMenuUseCases] used to integrate other features.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createDownloadLinkCandidate(
    context: Context,
    contextMenuUseCases: ContextMenuUseCases,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.download_link",
    label = context.getString(R.string.mozac_feature_contextmenu_download_link),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isLinkForOtherThanWebpage() && additionalValidation(
            tab, hitResult
        )
    },
    action = { tab, hitResult ->
        contextMenuUseCases.injectDownload(
            tab.id,
            DownloadState(
                hitResult.getLink(),
                skipConfirmation = true,
                private = tab.content.private,
                referrerUrl = tab.content.url,
            ),
        )
    },
)

/**
 * Context Menu item: "Share Link".
 *
 * @param context [Context] used for various system interactions.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createShareLinkCandidate(
    context: Context,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.share_link",
    label = context.getString(R.string.mozac_feature_contextmenu_share_link),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && (hitResult.isUri() || hitResult.isImage() || hitResult.isVideoAudio()) && additionalValidation(
            tab, hitResult
        )
    },
    action = { _, hitResult ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_TEXT, hitResult.getLink())
        }

        try {
            context.startActivity(
                intent.createChooserExcludingCurrentApp(
                    context,
                    context.getString(R.string.mozac_feature_contextmenu_share_link),
                ),
            )
        } catch (e: ActivityNotFoundException) {
            Log.log(
                Log.Priority.WARN,
                message = "No activity to share to found",
                throwable = e,
                tag = "createShareLinkCandidate",
            )
        }
    },
)

/**
 * Context Menu item: "Share image"
 *
 * @param context [Context] used for various system interactions.
 * @param contextMenuUseCases [ContextMenuUseCases] used to integrate other features.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createShareImageCandidate(
    context: Context,
    contextMenuUseCases: ContextMenuUseCases,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.share_image",
    label = context.getString(R.string.mozac_feature_contextmenu_share_image),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isImage() && additionalValidation(
            tab, hitResult
        )
    },
    action = { tab, hitResult ->
        contextMenuUseCases.injectShareFromInternet(
            tab.id,
            ShareInternetResourceState(
                url = hitResult.src,
                private = tab.content.private,
                referrerUrl = tab.content.url,
            ),
        )
    },
)

/**
 * Context Menu item: "Copy Link".
 *
 * @param context [Context] used for various system interactions.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createCopyLinkCandidate(
    context: Context,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.copy_link",
    label = context.getString(R.string.mozac_feature_contextmenu_copy_link),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && (hitResult.isUri() || hitResult.isImage() || hitResult.isVideoAudio()) && additionalValidation(
            tab, hitResult
        )
    },
    action = { _, hitResult ->
        clipPlainText(
            context,
            hitResult.getLink(),
            hitResult.getLink(),
            R.string.mozac_feature_contextmenu_snackbar_link_copied,
        )
    },
)

/**
 * Context Menu item: "Copy Image Location".
 *
 * @param context [Context] used for various system interactions.
 * @param additionalValidation Callback for the final validation in deciding whether this menu option
 * will be shown. Will only be called if all the intrinsic validations passed.
 */
fun createCopyImageLocationCandidate(
    context: Context,
    additionalValidation: (SessionState, HitResult) -> Boolean = { _, _ -> true },
) = ContextMenuCandidate(
    id = "mozac.feature.contextmenu.copy_image_location",
    label = context.getString(R.string.mozac_feature_contextmenu_copy_image_location),
    showFor = { tab, hitResult ->
        tab.isUrlSchemeAllowed(hitResult.getLink()) && hitResult.isImage() && additionalValidation(
            tab, hitResult
        )
    },
    action = { _, hitResult ->
        clipPlainText(
            context,
            hitResult.getLink(),
            hitResult.src,
            R.string.mozac_feature_contextmenu_snackbar_link_copied,
        )
    },
)

private fun clipPlainText(
    context: Context,
    label: String,
    plainText: String,
    displayTextId: Int,
) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, plainText)
    clipboardManager.setPrimaryClip(clip)

    // todo: show snackbar
//    snackbarDelegate.show(
//        snackBarParentView = snackBarParentView,
//        text = displayTextId,
//        duration = Snackbar.LENGTH_SHORT,
//    )
}


// Some helper methods to work with HitResult. We may want to improve the API of HitResult and remove some of the
// helpers eventually: https://github.com/mozilla-mobile/android-components/issues/1443

private fun HitResult.isImage(): Boolean =
    (this is HitResult.IMAGE || this is HitResult.IMAGE_SRC) && src.isNotEmpty()

private fun HitResult.isVideoAudio(): Boolean =
    (this is HitResult.VIDEO || this is HitResult.AUDIO) && src.isNotEmpty()

private fun HitResult.isUri(): Boolean =
    ((this is HitResult.UNKNOWN && src.isNotEmpty()) || this is HitResult.IMAGE_SRC)

private fun HitResult.isHttpLink(): Boolean = isUri() && getLink().startsWith("http")

private fun HitResult.isLinkForOtherThanWebpage(): Boolean {
    val link = getLink()
    val isHtml = link.endsWith("html") || link.endsWith("htm")
    return isHttpLink() && !isHtml
}

private fun HitResult.isIntent(): Boolean =
    (this is HitResult.UNKNOWN && src.isNotEmpty() && getLink().startsWith("intent:"))

private fun HitResult.isMailto(): Boolean =
    (this is HitResult.UNKNOWN && src.isNotEmpty()) && getLink().startsWith("mailto:")

private fun HitResult.canOpenInExternalApp(appLinksUseCases: AppLinksUseCases): Boolean {
    if (isHttpLink() || isIntent() || isVideoAudio()) {
        val redirect = appLinksUseCases.appLinkRedirectIncludeInstall(getLink())
        return redirect.hasExternalApp() || redirect.hasMarketplaceIntent()
    }
    return false
}

internal fun HitResult.getLink(): String = when (this) {
    is HitResult.UNKNOWN -> src
    is HitResult.IMAGE_SRC -> uri
    is HitResult.IMAGE -> if (title.isNullOrBlank()) {
        src.takeOrReplace(MAX_TITLE_LENGTH, "image")
    } else {
        title.toString()
    }

    is HitResult.VIDEO -> if (title.isNullOrBlank()) src else title.toString()

    is HitResult.AUDIO -> if (title.isNullOrBlank()) src else title.toString()

    else -> "about:blank"
}

@VisibleForTesting
internal fun SessionState.isUrlSchemeAllowed(url: String): Boolean {
    return when (val engineSession = engineState.engineSession) {
        null -> true
        else -> {
            val urlScheme = Uri.parse(url).normalizeScheme().scheme
            !engineSession.getBlockedSchemes().contains(urlScheme)
        }
    }
}