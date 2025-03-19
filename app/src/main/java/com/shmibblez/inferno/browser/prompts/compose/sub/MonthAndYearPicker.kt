package com.shmibblez.inferno.browser.prompts.compose.sub

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.util.Date
import com.shmibblez.inferno.R
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.ext.year
import java.util.Calendar

@Composable
fun rememberMonthAndYearPickerState(monthState: NumberPickerState,yearState: NumberPickerState) = remember { MonthAndYearPickerState(
    month = monthState,
    year = yearState,
    ) }

data class MonthAndYearPickerState(
    val month: NumberPickerState,
    val year: NumberPickerState,
)

@Composable
fun MonthAndYearPicker(state: MonthAndYearPickerState) {
    val context = LocalContext.current
    val months = remember { context.resources.getStringArray(R.array.mozac_feature_prompts_months).toList() }
    Row {
        NumberPicker(
            state = state.month,
            values =  months,
            modifier = Modifier.weight(1F),
            selected = months[1],
        )
//        val currentYear = Date().year
        val currentYear = Calendar.getInstance().year
        NumberPicker(
            state = state.year,
            values = (0..(currentYear + 50)).map { it.toString() },
            modifier = Modifier.weight(1F),
            selected = currentYear.toString(),
        )
    }
}