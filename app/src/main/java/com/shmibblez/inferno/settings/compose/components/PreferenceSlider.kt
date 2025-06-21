package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.unit.dp
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
        verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING),
    ) {
        // text
        InfernoText(
            text = text, fontColor = when (enabled) {
                true -> context.infernoTheme().value.primaryTextColor
                false -> context.infernoTheme().value.secondaryTextColor
            }
        )

        // subtitle
        InfernoText(
            text = summary,
            infernoStyle = InfernoTextStyle.SmallSecondary,
        )

        // slider
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING),
        ) {
            // spacer
            Spacer(Modifier.height(PrefUiConst.PREFERENCE_VERTICAL_PADDING * 2F))

            // slider
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                modifier = Modifier.weight(1F),
                enabled = enabled,
                valueRange = 0.5F..2F,
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
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.CenterHorizontally)
//                            .background(
//                                color = when (enabled) {
//                                    true -> context.infernoTheme().value.primaryActionColor
//                                    false -> context.infernoTheme().value.secondaryBackgroundColor
//                                },
//                                shape = CircleShape,
//                            )
                            .wrapContentSize(unbounded = true),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                                .background(
                                    color = when (enabled) {
                                        true -> context.infernoTheme().value.primaryActionColor
                                        false -> context.infernoTheme().value.secondaryBackgroundColor
                                    },
                                    shape = CircleShape,
                                ),
                        )
                    }
                },
            )

            // spacer
            Spacer(Modifier.height(PrefUiConst.PREFERENCE_VERTICAL_PADDING * 2F))

            // current val
            InfernoText(text = "${(sliderPosition * 100F).toInt()}%")
        }
    }
}