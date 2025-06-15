/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.browser.prompts.webPrompts

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import com.shmibblez.inferno.browser.prompts.InfernoWebPrompterState
import mozilla.components.feature.prompts.PromptFeature

/**
 * Provides functionality for picking photos from the device's gallery using native picker.
 *
 * @property context The application [Context].
 * @property singleMediaPicker An [ActivityResultLauncher] for picking a single photo.
 * @property multipleMediaPicker An [ActivityResultLauncher] for picking multiple photos.
 */
class InfernoAndroidPhotoPicker(
    val context: Context,
    val singleMediaPicker: ActivityResultLauncher<PickVisualMediaRequest>,
    val multipleMediaPicker: ActivityResultLauncher<PickVisualMediaRequest>,
) {
    internal val isPhotoPickerAvailable =
        ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)

    companion object {
        /**
         * Registers a photo picker activity launcher in single-select mode.
         * Note that you must call singleMediaPicker before the fragment is created.
         *
         * @param getFragment A function that returns the [Fragment] which hosts the file picker.
         * @param getWebPromptState A function that returns the [PromptFeature]
         * that handles the result of the photo picker.
         * @return An [ActivityResultLauncher] for picking a single photo.
         */
        fun singleMediaPicker(
            getFragment: () -> Fragment,
            getWebPromptState: () -> InfernoWebPrompterState?,
        ): ActivityResultLauncher<PickVisualMediaRequest> {
            return getFragment.invoke()
                .registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    uri?.let {
                        getWebPromptState.invoke()?.onAndroidPhotoPickerResult(arrayOf(uri))
                    }
                }
//            return rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//                uri?.let {
//                    getWebPromptState.invoke()?.onAndroidPhotoPickerResult(arrayOf(uri))
//                }
//            }
        }

        /**
         * Registers a photo picker activity launcher in single-select mode.
         * Note that you must call singleMediaPicker before the fragment is created.
         *
         * @param getFragment A function that returns the [Fragment] which hosts the file picker.
         * @param getWebPromptState A function that returns the [PromptFeature]
         * that handles the result of the photo picker.
         * @return An [ActivityResultLauncher] for picking a single photo.
         */
        @Composable
        fun singleMediaPicker(
            getWebPromptState: () -> InfernoWebPrompterState?,
        ): ActivityResultLauncher<PickVisualMediaRequest> {
//            return getFragment.invoke()
//                .registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//                    uri?.let {
//                        getWebPromptState.invoke()?.onAndroidPhotoPickerResult(arrayOf(uri))
//                    }
//                }
            return rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    getWebPromptState.invoke()?.onAndroidPhotoPickerResult(arrayOf(uri))
                }
            }
        }

        /**
         * Registers a photo picker activity launcher in multi-select mode.
         * Note that you must call multipleMediaPicker before the fragment is created.
         *
         * @param getFragment A function that returns the [Fragment] which hosts the file picker.
         * @param getWebPromptState A function that returns the [PromptFeature]
         * that handles the result of the photo picker.
         * @return An [ActivityResultLauncher] for picking multiple photos.
         */
        fun multipleMediaPicker(
            getFragment: () -> Fragment,
            getWebPromptState: () -> InfernoWebPrompterState?,
        ): ActivityResultLauncher<PickVisualMediaRequest> {
            return getFragment.invoke()
                .registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uriList ->
                    getWebPromptState.invoke()?.onAndroidPhotoPickerResult(uriList.toTypedArray())
                }
//            return rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uriList ->
//                getWebPromptState.invoke()?.onAndroidPhotoPickerResult(uriList.toTypedArray())
//            }
        }

        /**
         * Registers a photo picker activity launcher in multi-select mode.
         * Note that you must call multipleMediaPicker before the fragment is created.
         *
         * @param getWebPromptState A function that returns the [PromptFeature]
         * that handles the result of the photo picker.
         * @return An [ActivityResultLauncher] for picking multiple photos.
         */
        @Composable
        fun multipleMediaPicker(
            getWebPromptState: () -> InfernoWebPrompterState?,
        ): ActivityResultLauncher<PickVisualMediaRequest> {
//            return getFragment.invoke()
//                .registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uriList ->
//                    getWebPromptState.invoke()?.onAndroidPhotoPickerResult(uriList.toTypedArray())
//                }
            return rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uriList ->
                getWebPromptState.invoke()?.onAndroidPhotoPickerResult(uriList.toTypedArray())
            }
        }
    }
}
