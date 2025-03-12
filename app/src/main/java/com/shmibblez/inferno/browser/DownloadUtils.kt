/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.browser

import android.content.Context
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.browser.state.state.content.DownloadState.Status
import mozilla.components.concept.toolbar.ScrollableToolbar
import mozilla.components.feature.downloads.AbstractFetchDownloadService
import com.shmibblez.inferno.downloads.dialog.DynamicDownloadDialog

internal fun handleOnDownloadFinished(
    context: Context,
    downloadState: DownloadState,
    downloadJobStatus: Status,
    tryAgain: (String) -> Unit,
    browserToolbars: List<ScrollableToolbar>,
) {
    // If the download is just paused, don't show any in-app notification
    if (shouldShowCompletedDownloadDialog(downloadState, downloadJobStatus, context)) {
        val safeContext = context ?: return
        val onCannotOpenFile: (DownloadState) -> Unit = {
            // todo: snackbar
//            showCannotOpenFileError(binding.dynamicSnackbarContainer, safeContext, it)
        }
        if (downloadState.openInApp && downloadJobStatus == Status.COMPLETED) {
            val fileWasOpened = AbstractFetchDownloadService.openFile(
                applicationContext = safeContext.applicationContext,
                download = downloadState,
            )
            if (!fileWasOpened) {
                onCannotOpenFile(downloadState)
            }
        } else {
            saveDownloadDialogState(
                downloadState.sessionId,
                downloadState,
                downloadJobStatus,
            )

            // todo: dialog for downloads
//            val dynamicDownloadDialog = DynamicDownloadDialog(
//                context = safeContext,
//                downloadState = downloadState,
//                didFail = downloadJobStatus == Status.FAILED,
//                tryAgain = tryAgain,
//                onCannotOpenFile = onCannotOpenFile,
//                binding = binding.viewDynamicDownloadDialog,
//            ) { sharedViewModel.downloadDialogState.remove(downloadState.sessionId) }
//
//            dynamicDownloadDialog.show()
            browserToolbars.forEach { it.expand() }
        }
    }
}
