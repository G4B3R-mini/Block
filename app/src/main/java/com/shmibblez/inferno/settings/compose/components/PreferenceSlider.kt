package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceSlider(
    text: String,
    summary: String,
    initialPosition: Float,
    onSet: (Float) -> Unit,
    enabled: Boolean,
) {
    val context = LocalContext.current
    var sliderPosition by remember { mutableFloatStateOf(initialPosition) }

    // when setting changed, update to reflect value
    LaunchedEffect(initialPosition) {
        sliderPosition = initialPosition
    }

    Column(
        modifier = Modifier.padding(
            horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
            vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
        ),
        horizontalAlignment = Alignment.Start,
    ) {
        // text
        InfernoText(
            text = text,
            fontColor = when (enabled) {
                true -> context.infernoTheme().value.primaryTextColor
                false -> context.infernoTheme().value.secondaryTextColor
            }
        )

        // title
        if (enabled) {
            InfernoText(
                text = summary,
                infernoStyle = InfernoTextStyle.Subtitle,
            )
        }

        // slider
        Column(
            modifier = Modifier.padding(top = PrefUiConst.PREFERENCE_VERTICAL_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
        ) {
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                modifier = Modifier.weight(1F),
                enabled = enabled,
                valueRange = 50F..200F,
                // 5% intervals
                steps = 29,
                onValueChangeFinished = { onSet.invoke(sliderPosition) },
                colors = SliderColors(
                    thumbColor = context.infernoTheme().value.primaryActionColor,
                    activeTrackColor = context.infernoTheme().value.primaryActionColor,
                    activeTickColor = context.infernoTheme().value.primaryActionColor,
                    inactiveTrackColor = context.infernoTheme().value.secondaryBackgroundColor,
                    inactiveTickColor = context.infernoTheme().value.secondaryBackgroundColor,
                    disabledThumbColor = context.infernoTheme().value.secondaryBackgroundColor,
                    disabledActiveTrackColor = context.infernoTheme().value.secondaryBackgroundColor,
                    disabledActiveTickColor = context.infernoTheme().value.secondaryBackgroundColor,
                    disabledInactiveTrackColor = context.infernoTheme().value.secondaryBackgroundColor,
                    disabledInactiveTickColor = context.infernoTheme().value.secondaryBackgroundColor,
                ),
                thumb = {
                    Box(
                        modifier = Modifier.background(
                            color = context.infernoTheme().value.primaryActionColor,
                            shape = CircleShape,
                        )
                    )
                },
            )
            InfernoText(text = "$sliderPosition%")
        }
    }
}