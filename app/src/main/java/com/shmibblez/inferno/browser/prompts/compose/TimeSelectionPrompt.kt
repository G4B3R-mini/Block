package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.compose.sub.MonthAndYearPicker
import com.shmibblez.inferno.browser.prompts.compose.sub.rememberMonthAndYearPickerState
import com.shmibblez.inferno.browser.prompts.compose.sub.rememberNumberPickerState
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.ext.hour
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.ext.minute
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.ext.month
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.ext.year
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import java.util.Calendar
import java.util.Date

// todo: not tested

const val SELECTION_TYPE_DATE = 1
const val SELECTION_TYPE_DATE_AND_TIME = 2
const val SELECTION_TYPE_TIME = 3
const val SELECTION_TYPE_MONTH = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionPrompt(timeData: PromptRequest.TimeSelection, sessionId: String, type: Int) {
    val store = LocalContext.current.components.core.store
    val context = LocalContext.current
    val cal = Calendar.getInstance()
    val timeState = rememberTimePickerState()
    val datePickerState = rememberDatePickerState()
    val monthState = rememberNumberPickerState()
    val yearState = rememberNumberPickerState()
    val monthAndYearPickerState = rememberMonthAndYearPickerState(
        monthState = monthState,
        yearState = yearState,
    )

    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(timeData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, timeData))
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_prompts_cancel),
            action = {
                onNegativeAction(timeData)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, timeData))
            },
        ),
        neutralAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_prompts_clear),
            action = {
                onNeutralAction(timeData)
            },
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(
                when (type) {
                    SELECTION_TYPE_TIME -> android.R.string.ok
                    SELECTION_TYPE_DATE -> android.R.string.ok
                    SELECTION_TYPE_DATE_AND_TIME -> R.string.mozac_feature_prompts_set_date
                    SELECTION_TYPE_MONTH -> R.string.mozac_feature_prompts_set_date
                    else -> {
                        -1
                    }
                }
            ),
            action = {
                when (type) {
                    SELECTION_TYPE_TIME -> {
                        cal.hour = timeState.hour
                        cal.minute = timeState.minute
                        onPositiveAction(timeData, cal.time)
                        store.dispatch(
                            ContentAction.ConsumePromptRequestAction(
                                sessionId, timeData
                            )
                        )
                    }

                    SELECTION_TYPE_DATE -> {
                        cal.time = Date(datePickerState.selectedDateMillis ?: Date().time)
                        onPositiveAction(timeData, cal.time)
                        store.dispatch(
                            ContentAction.ConsumePromptRequestAction(
                                sessionId, timeData
                            )
                        )
                    }

                    SELECTION_TYPE_DATE_AND_TIME -> {
                        cal.time = Date(datePickerState.selectedDateMillis ?: Date().time)
                        cal.hour = timeState.hour
                        cal.minute = timeState.minute
                        onPositiveAction(timeData, cal.time)
                        store.dispatch(
                            ContentAction.ConsumePromptRequestAction(
                                sessionId, timeData
                            )
                        )
                    }

                    SELECTION_TYPE_MONTH -> {
                        cal.month =
                            context.resources.getStringArray(R.array.mozac_feature_prompts_months)
                                .indexOf(monthAndYearPickerState.month.selectedItem) - 1
                        cal.year = monthAndYearPickerState.year.selectedItem.toInt()
                        onPositiveAction(timeData, cal.time)
                        store.dispatch(
                            ContentAction.ConsumePromptRequestAction(
                                sessionId, timeData
                            )
                        )
                    }

                    else -> {}
                }
            },
        ),
    ) {
        when (type) {
            SELECTION_TYPE_TIME -> {
                TimeInput(timeState)
            }

            SELECTION_TYPE_DATE -> {
                DatePicker(datePickerState)
            }

            SELECTION_TYPE_DATE_AND_TIME -> {
                DatePicker(datePickerState)
                TimeInput(timeState)
            }

            SELECTION_TYPE_MONTH -> {
                MonthAndYearPicker(monthAndYearPickerState)
            }
        }
    }
}