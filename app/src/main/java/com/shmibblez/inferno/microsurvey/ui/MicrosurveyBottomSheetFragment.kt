/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.microsurvey.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.messaging.MicrosurveyMessageController
import com.shmibblez.inferno.microsurvey.ui.ext.toMicrosurveyUIData
import com.shmibblez.inferno.theme.FirefoxTheme

/**
 * A bottom sheet fragment for displaying a microsurvey.
 */
class MicrosurveyBottomSheetFragment : BottomSheetDialogFragment() {

    private val args by navArgs<MicrosurveyBottomSheetFragmentArgs>()

    private val microsurveyMessageController by lazy {
        MicrosurveyMessageController(requireComponents.appStore, (activity as HomeActivity))
    }

    private val closeBottomSheet = { dismiss() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                val bottomSheet = findViewById<View?>(R.id.design_bottom_sheet)
                bottomSheet?.setBackgroundResource(android.R.color.transparent)
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.setPeekHeightToHalfScreenHeight()
                behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        val messaging = context.components.nimbus.messaging
        val microsurveyId = args.microsurveyId

        lifecycleScope.launch {
            val microsurveyUIData = messaging.getMessage(microsurveyId)?.toMicrosurveyUIData()
            microsurveyUIData?.let {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                microsurveyMessageController.onMicrosurveyShown(it.id)
                setContent {
                    FirefoxTheme {
                        val activity = requireActivity() as HomeActivity

                        MicrosurveyBottomSheet(
                            question = it.question,
                            icon = it.icon,
                            answers = it.answers,
                            onPrivacyPolicyLinkClick = {
                                closeBottomSheet()
                                microsurveyMessageController.onPrivacyPolicyLinkClicked(
                                    it.id,
                                    it.utmContent,
                                )
                            },
                            onCloseButtonClicked = {
                                microsurveyMessageController.onMicrosurveyDismissed(it.id)
                                context.settings().shouldShowMicrosurveyPrompt = false
                                activity.isMicrosurveyPromptDismissed.value = true
                                closeBottomSheet()
                            },
                            onSubmitButtonClicked = { answer ->
                                context.settings().shouldShowMicrosurveyPrompt = false
                                activity.isMicrosurveyPromptDismissed.value = true
                                microsurveyMessageController.onSurveyCompleted(it.id, answer)
                            },
                        )
                    }
                }
            }
        }
    }

    private fun BottomSheetBehavior<View>.setPeekHeightToHalfScreenHeight() {
        peekHeight = resources.displayMetrics.heightPixels / 2
    }
}
