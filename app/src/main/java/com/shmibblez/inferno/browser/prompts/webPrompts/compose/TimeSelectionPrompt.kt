package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.sub.MonthAndYearPicker
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.sub.rememberMonthAndYearPickerState
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.sub.rememberNumberPickerState
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onNegativeAction
import com.shmibblez.inferno.browser.prompts.onNeutralAction
import com.shmibblez.inferno.browser.prompts.onPositiveAction
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

// todo:
//   - date picker not scrolling up when horizontal
//   - modal sheet expanded when keyboard pops up
//   - error when press ok and nothing selected
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
        Column(
            modifier = Modifier
                .scrollable(rememberScrollState(0), orientation = Orientation.Vertical),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (type) {
                SELECTION_TYPE_TIME -> {
                    TimeInput(
                        timeState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color.White,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color.LightGray,
                            selectorColor = Color.White,
                            containerColor = Color.Black,
                            periodSelectorBorderColor = Color.White,
                            periodSelectorSelectedContainerColor = Color.Red,
                            periodSelectorUnselectedContainerColor = Color.Black,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = Color.LightGray,
                            timeSelectorSelectedContainerColor = Color.Black,
                            timeSelectorUnselectedContainerColor = Color.Black,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContentColor = Color.LightGray,
                        ),
                    )
                }

                SELECTION_TYPE_DATE -> {
                    DatePicker(
                        datePickerState,
                        modifier = Modifier.padding(bottom = 16.dp),
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.Black,
                            titleContentColor = Color.White,
                            headlineContentColor = Color.White,
                            weekdayContentColor = Color.White,
                            subheadContentColor = Color.White,
                            navigationContentColor = Color.White,
                            yearContentColor = Color.White,
                            disabledYearContentColor = Color.LightGray,
                            currentYearContentColor = Color.White,
                            selectedYearContentColor = Color.White,
                            disabledSelectedYearContentColor = Color.LightGray,
                            selectedYearContainerColor = Color.Black,
                            disabledSelectedYearContainerColor = Color.Black,
                            dayContentColor = Color.White,
                            disabledDayContentColor = Color.LightGray,
                            selectedDayContentColor = Color.White,
                            disabledSelectedDayContentColor = Color.LightGray,
                            selectedDayContainerColor = Color.Black,
                            disabledSelectedDayContainerColor = Color.LightGray,
                            todayContentColor = Color.White,
                            todayDateBorderColor = Color.White,
                            dayInSelectionRangeContentColor = Color.White,
                            dayInSelectionRangeContainerColor = Color.White,
                            dividerColor = Color.White,
                            dateTextFieldColors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.LightGray,
                                disabledLabelColor = Color.LightGray,
                                errorLabelColor = Color.Red,
                                focusedPlaceholderColor = Color.LightGray,
                                unfocusedPlaceholderColor = Color.LightGray,
                                disabledPlaceholderColor = Color.Gray,
                                errorPlaceholderColor = Color.LightGray,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                errorContainerColor = Color.Black,
                                disabledContainerColor = Color.Black,
                                errorTextColor = Color.Red,
                                errorCursorColor = Color.Red,
                                errorIndicatorColor = Color.Red,
                            ),
                        ),
                    )
                }

                SELECTION_TYPE_DATE_AND_TIME -> {
                    TimeInput(
                        timeState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color.White,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color.LightGray,
                            selectorColor = Color.White,
                            containerColor = Color.Black,
                            periodSelectorBorderColor = Color.White,
                            periodSelectorSelectedContainerColor = Color.Red,
                            periodSelectorUnselectedContainerColor = Color.Black,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = Color.LightGray,
                            timeSelectorSelectedContainerColor = Color.Black,
                            timeSelectorUnselectedContainerColor = Color.Black,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContentColor = Color.LightGray,
                        ),
                    )
                    DatePicker(
                        datePickerState,
                        modifier = Modifier.padding(bottom = 16.dp),
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.Black,
                            titleContentColor = Color.White,
                            headlineContentColor = Color.White,
                            weekdayContentColor = Color.White,
                            subheadContentColor = Color.White,
                            navigationContentColor = Color.White,
                            yearContentColor = Color.White,
                            disabledYearContentColor = Color.LightGray,
                            currentYearContentColor = Color.White,
                            selectedYearContentColor = Color.White,
                            disabledSelectedYearContentColor = Color.LightGray,
                            selectedYearContainerColor = Color.Black,
                            disabledSelectedYearContainerColor = Color.Black,
                            dayContentColor = Color.White,
                            disabledDayContentColor = Color.LightGray,
                            selectedDayContentColor = Color.White,
                            disabledSelectedDayContentColor = Color.LightGray,
                            selectedDayContainerColor = Color.Black,
                            disabledSelectedDayContainerColor = Color.LightGray,
                            todayContentColor = Color.White,
                            todayDateBorderColor = Color.White,
                            dayInSelectionRangeContentColor = Color.White,
                            dayInSelectionRangeContainerColor = Color.White,
                            dividerColor = Color.White,
                            dateTextFieldColors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.LightGray,
                                disabledLabelColor = Color.LightGray,
                                errorLabelColor = Color.Red,
                                focusedPlaceholderColor = Color.LightGray,
                                unfocusedPlaceholderColor = Color.LightGray,
                                disabledPlaceholderColor = Color.Gray,
                                errorPlaceholderColor = Color.LightGray,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                errorContainerColor = Color.Black,
                                disabledContainerColor = Color.Black,
                                errorTextColor = Color.Red,
                                errorCursorColor = Color.Red,
                                errorIndicatorColor = Color.Red,
                            ),
                        ),
                    )
                }

                SELECTION_TYPE_MONTH -> {
                    MonthAndYearPicker(monthAndYearPickerState)
                }
            }
        }
    }
}