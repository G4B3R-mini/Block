package com.shmibblez.inferno.browser.prompts

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.net.toUri
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.download.compose.DownloadAppChooserPrompt
import com.shmibblez.inferno.browser.prompts.download.compose.DownloadPrompt
import com.shmibblez.inferno.browser.prompts.download.compose.DynamicDownloadPrompt
import com.shmibblez.inferno.browser.prompts.download.compose.FirstPartyDownloadPrompt
import com.shmibblez.inferno.browser.prompts.download.compose.ThirdPartyDownloadPrompt
import com.shmibblez.inferno.ext.realFilenameOrGuessed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.browser.state.state.content.DownloadState.Status
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.downloads.ContentSize
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.downloads.Filename
import mozilla.components.feature.downloads.manager.AndroidDownloadManager
import mozilla.components.feature.downloads.manager.DownloadManager
import mozilla.components.feature.downloads.ui.DownloaderApp
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.OnNeedToRequestPermissions
import mozilla.components.support.ktx.android.content.appName
import mozilla.components.support.ktx.android.content.isPermissionGranted
import mozilla.components.support.ktx.kotlin.isSameOriginAs
import mozilla.components.support.utils.Browsers


// todo:
//   - permission handling: set permissions callback in parent
//   - check each prompt to make sure layout and functionality ok
//   - what to do on onDownloadStopped, where to call, check in [DownloadsFeature]
//   - test

private data class StoppedDownloadState(
    val downloadState: DownloadState,
    val downloadJobStatus: Status,
)


/**
 * Shows prompts when download requested and finished
 */
@Composable
fun DownloadComponent(
    applicationContext: Context,
    store: BrowserStore,
    useCases: DownloadsUseCases,
    onNeedToRequestPermissions: OnNeedToRequestPermissions = { },
    onCannotOpenFile: (DownloadState) -> Unit,
    downloadManager: DownloadManager = AndroidDownloadManager(
        applicationContext, store
    ),
    customTabSessionId: String? = null,
//    promptsStyling: PromptsStyling? = null,
    shouldForwardToThirdParties: () -> Boolean = { false },
    useCustomFirstPartyDownloadPrompt: Boolean = true,
    useCustomThirdPartyDownloadDialog: Boolean = true, // ((List<DownloaderApp>, (DownloaderApp) -> Unit, () -> Unit) -> Unit)? = null,
    setOnPermissionsResultCallback: ((permissions: Array<String>, grantResults: IntArray) -> Unit) -> Unit,
) {
    // listeners
    var dismissPromptScope by remember { mutableStateOf<CoroutineScope?>(null) }
    var scope by remember { mutableStateOf<CoroutineScope?>(null) }

    var previousTab by remember { mutableStateOf<SessionState?>(null) }
    var downloadState by remember { mutableStateOf<DownloadState?>(null) }
    var apps by remember { mutableStateOf<List<DownloaderApp>>(emptyList()) }
    var shouldShowAppDownloaderDialog by remember { mutableStateOf(false) }
    // persisted with parent activity (as long as app is alive)
    // todo: need custom saver in order to persist
//    val stoppedDownloadStates: SnapshotStateMap<String, StoppedDownloadState> = rememberSaveable { mutableStateMapOf() }
    val stoppedDownloadStates: SnapshotStateMap<String, StoppedDownloadState> =
        remember { mutableStateMapOf() }


    /**
     * Find all apps that can perform a download, including this app.
     */
    @VisibleForTesting
    fun getDownloaderApps(context: Context, download: DownloadState): List<DownloaderApp> {
        val packageManager = context.packageManager

        val browsers = Browsers.findResolvers(context, packageManager, includeThisApp = true)
            .associateBy { it.activityInfo.identifier }

        val thisApp =
            browsers.values.firstOrNull { it.activityInfo.packageName == context.packageName }
                ?.toDownloaderApp(context, download)

        // Check for data URL that can cause a TransactionTooLargeException when querying for apps
        // See https://github.com/mozilla-mobile/android-components/issues/9665
        if (download.url.startsWith("data:")) {
            return listOfNotNull(thisApp)
        }

        val appResolvers = Browsers.findResolvers(
            context,
            packageManager,
            includeThisApp = false,
            url = download.url,
            contentType = download.contentType,
        )
        // Remove browsers and returns only the apps that can perform a download plus this app.
        return appResolvers.filter { !browsers.contains(it.activityInfo.identifier) }
            .map { it.toDownloaderApp(context, download) } + listOfNotNull(thisApp)
    }

    @VisibleForTesting(otherwise = PRIVATE)
    fun showDownloadNotSupportedError() {
        Toast.makeText(
            applicationContext,
            applicationContext.getString(
                R.string.mozac_feature_downloads_file_not_supported2,
                applicationContext.appName,
            ),
            Toast.LENGTH_LONG,
        ).show()
    }

    @VisibleForTesting
    fun startDownload(download: DownloadState): Boolean {
        val id = downloadManager.download(download)
        return if (id != null) {
            true
        } else {
            showDownloadNotSupportedError()
            false
        }
    }

    @VisibleForTesting
    fun onDownloaderAppSelected(
        app: DownloaderApp, tab: SessionState, download: DownloadState,
    ) {
        if (app.packageName == applicationContext.packageName) {
            if (applicationContext.isPermissionGranted(downloadManager.permissions.asIterable())) {
                startDownload(download)
                useCases.consumeDownload(tab.id, download.id)
            } else {
                onNeedToRequestPermissions(downloadManager.permissions)
            }
        } else {
            try {
                applicationContext.startActivity(app.toIntent())
            } catch (error: ActivityNotFoundException) {
                val errorMessage = applicationContext.getString(
                    R.string.mozac_feature_downloads_unable_to_open_third_party_app,
                    app.name,
                )
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }
            useCases.consumeDownload(tab.id, download.id)
        }
    }

    // todo: was once used to not replace previous download request, maybe in future add list of
    //  requests and handle in order, similar to PromptComponent
//    fun findPreviousDownloadDialogFragment(): DownloadDialogFragment? {
//        return fragmentManager?.findFragmentByTag(DownloadDialogFragment.FRAGMENT_TAG) as? DownloadDialogFragment
//    }

    fun withActiveDownload(block: (Pair<SessionState, DownloadState>) -> Unit) {
        val state = store.state.findTabOrCustomTabOrSelectedTab(customTabSessionId) ?: return
        val download = state.content.download ?: return
        block(Pair(state, download))
    }

    @VisibleForTesting
    fun showPermissionDeniedDialog() {
        // todo: show dialog for permission denied
//        fragmentManager?.let {
//            val dialog = DeniedPermissionDialogFragment.newInstance(
//                R.string.mozac_feature_downloads_write_external_storage_permissions_needed_message,
//            )
//            dialog.showNow(fragmentManager, DeniedPermissionDialogFragment.FRAGMENT_TAG)
//        }
    }

    /**
     * Notifies the feature that the permissions request was completed. It will then
     * either trigger or clear the pending download.
     */
    fun onPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        if (permissions.isEmpty()) {
            // If we are requesting permissions while a permission prompt is already being displayed
            // then Android seems to call `onPermissionsResult` immediately with an empty permissions
            // list. In this case just ignore it.
            return
        }

        withActiveDownload { (tab, download) ->
            if (applicationContext.isPermissionGranted(downloadManager.permissions.asIterable())) {
                if (shouldForwardToThirdParties()) {
                    startDownload(download)
                    useCases.consumeDownload(tab.id, download.id)
                } else {
                    previousTab = tab
                    downloadState = download
                    // processDownload(tab, download) equivalent, state rebuilt with new values
                }
            } else {
                useCases.cancelDownloadRequest.invoke(tab.id, download.id)
                showPermissionDeniedDialog()
            }
        }
    }

    /**
     * dismisses current download dialog
     */
    fun dismissDialog() {
        previousTab = null
        downloadState = null
    }

    // todo: prompt style based on app colors
//    /**
//     * Styling for the download dialog prompt
//     */
//    data class PromptsStyling(
//        val gravity: Int,
//        val shouldWidthMatchParent: Boolean = false,
//        @ColorRes val positiveButtonBackgroundColor: Int? = null,
//        @ColorRes val positiveButtonTextColor: Int? = null,
//        val positiveButtonRadius: Float? = null,
//        val fileNameEndMargin: Int? = null,
//    )

    // set setOnPermissionsResultCallback callback
    LaunchedEffect(setOnPermissionsResultCallback) {
        // sets callback in parent so can be invoked
        setOnPermissionsResultCallback.invoke { permissions, grantResults ->
            onPermissionsResult(
                permissions, grantResults
            )
        }
    }
    // set OnDownloadStopped callback
    LaunchedEffect(downloadManager) {
        // todo: not workin
        downloadManager.onDownloadStopped = { state, _, status ->
            stoppedDownloadStates[state.id] = StoppedDownloadState(state, status)
        }
    }

    DisposableEffect(null) {
        // Dismiss the previous prompts when the user navigates to another site.
        // This prevents prompts from the previous page from covering content.
        dismissPromptScope = store.flowScoped { flow ->
            flow.mapNotNull { state -> state.findTabOrCustomTabOrSelectedTab(customTabSessionId) }
                .distinctUntilChangedBy { it.content.url }.collect {
                    val currentHost = previousTab?.content?.url
                    val newHost = it.content.url

                    // The user is navigating to another site
                    if (currentHost?.isSameOriginAs(newHost) == false) {
                        previousTab?.let { tab ->
                            // We have an old download request.
                            tab.content.download?.let { download ->
                                useCases.cancelDownloadRequest.invoke(tab.id, download.id)
                                // dismissed when previousTab set to null, replaces dismissAllDownloadDialogs()
                                previousTab = null
                            }
                        }
                    }
                }
        }

        scope = store.flowScoped { flow ->
            flow.mapNotNull { state -> state.findTabOrCustomTabOrSelectedTab(customTabSessionId) }
                .distinctUntilChangedBy { it.content.download }.collect { state ->
                    state.content.download?.let { newDownloadState ->
                        previousTab = state
                        downloadState = newDownloadState
                        apps = getDownloaderApps(applicationContext, newDownloadState)
                        // We only show the dialog If we have multiple apps that can handle the download.
                        shouldShowAppDownloaderDialog =
                            shouldForwardToThirdParties() && apps.size > 1
                        // processDownload(state, newDownloadState) equivalent is called in composition below
                    }
                }
        }

        onDispose {
            scope?.cancel()
            dismissPromptScope?.cancel()
            downloadManager.unregisterListeners()
        }
    }

    if (shouldShowAppDownloaderDialog && previousTab != null && downloadState != null) {
        when (useCustomThirdPartyDownloadDialog) {
            false -> {
                DownloadAppChooserPrompt(
                    downloaderApps = apps,
                    onAppSelected = { app ->
                        onDownloaderAppSelected(app, previousTab!!, downloadState!!)
                        dismissDialog()
                    },
                    onDismiss = {
//                            emitPromptDismissedFact()
                        useCases.cancelDownloadRequest.invoke(previousTab!!.id, downloadState!!.id)
                        dismissDialog()
                    },
                )
            } // replaces showAppDownloaderDialog(previousTab, download, apps)

            true -> {
                ThirdPartyDownloadPrompt(
                    downloaderApps = apps,
                    onAppSelected = {
                        onDownloaderAppSelected(it, previousTab!!, downloadState!!)
                        dismissDialog()
                    },
                    onDismiss = {
                        useCases.cancelDownloadRequest.invoke(previousTab!!.id, downloadState!!.id)
                        dismissDialog()
                    },
                )
            } // replaces customThirdPartyDownloadDialog.invoke(...)
        }
    } else {
        if (applicationContext.isPermissionGranted(downloadManager.permissions.asIterable()) && downloadState != null && previousTab != null) {
            when {
                useCustomFirstPartyDownloadPrompt && !downloadState!!.skipConfirmation -> {
                    FirstPartyDownloadPrompt(
                        filename = Filename(downloadState!!.realFilenameOrGuessed),
                        contentSize = ContentSize(
                            downloadState!!.contentLength ?: 0
                        ),
                        onPositiveAction = {
                            startDownload(downloadState!!)
                            useCases.consumeDownload.invoke(previousTab!!.id, downloadState!!.id)
                            dismissDialog()
                        },
                        onNegativeAction = {
                            useCases.cancelDownloadRequest.invoke(
                                previousTab!!.id, downloadState!!.id
                            )
                            dismissDialog()
                        },
                    ) // replaces customFirstPartyDownloadDialog.invoke(...)
                }

                !downloadState!!.skipConfirmation -> {
                    DownloadPrompt(
                        download = downloadState!!,
                        onStartDownload = {
                            startDownload(downloadState!!)
                            useCases.consumeDownload.invoke(previousTab!!.id, downloadState!!.id)
                            dismissDialog()
                        },
                        onCancelDownload = {
                            useCases.cancelDownloadRequest.invoke(
                                previousTab!!.id, downloadState!!.id
                            )
                            dismissDialog()
                        },
                    ) // replaces showDownloadDialog(previousTab, download)
                }

                else -> {
                    useCases.consumeDownload(previousTab!!.id, downloadState!!.id)
                    startDownload(downloadState!!)
                    dismissDialog()
                }
            }
        } else if (downloadState == null || previousTab == null) {
            // do nothing
        } else {
            onNeedToRequestPermissions(downloadManager.permissions)
        }
    }

    if (stoppedDownloadStates.isNotEmpty()) {
        for (entry in stoppedDownloadStates) {
            // If the download is just paused, don't show any in-app notification
            if (shouldShowCompletedDownloadDialog(
                    entry.value.downloadState, entry.value.downloadJobStatus, customTabSessionId
                )
            ) {
                DynamicDownloadPrompt(
                    downloadState = entry.value.downloadState,
                    didFail = entry.value.downloadJobStatus == Status.FAILED,
                    tryAgain = { downloadManager.tryAgain(entry.value.downloadState.id) },
                    onCannotOpenFile = onCannotOpenFile,
                    onDismiss = {
                        stoppedDownloadStates.remove(entry.key)
                    },
                )
            }
        }
    }
}

internal fun shouldShowCompletedDownloadDialog(
    downloadState: DownloadState,
    status: Status,
    tabId: String?,
): Boolean {
    val isValidStatus = status in listOf(Status.COMPLETED, Status.FAILED)
//    val isSameTab = downloadState.sessionId == (tabId ?: false)

    return isValidStatus // && isSameTab
}

// extensions

val ActivityInfo.identifier: String get() = packageName + name

@VisibleForTesting
fun DownloaderApp.toIntent(): Intent {
    return Intent(Intent.ACTION_VIEW).apply {
        setDataAndTypeAndNormalize(url.toUri(), contentType)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        setClassName(packageName, activityName)
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
}

@VisibleForTesting
fun ResolveInfo.toDownloaderApp(context: Context, download: DownloadState): DownloaderApp {
    return DownloaderApp(
        loadLabel(context.packageManager).toString(),
        this,
        activityInfo.packageName,
        activityInfo.name,
        download.url,
        download.contentType,
    )
}


