package com.shmibblez.inferno.browser.prompts.compose

import android.content.Context
import androidx.compose.foundation.lazy.items
import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.button.RadioButton
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.ColorItem
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.toColorItem
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.toHexColor


@Composable
fun ColorPickerDialogPrompt(colorData: PromptRequest.Color, sessionId: String) {
    val context = LocalContext.current
    val store = context.components.core.store
    var selectedColor by remember { mutableIntStateOf(colorData.defaultColor.toColorInt()) }
    val (initiallySelectedCustomColor, setInitiallySelectedCustomColor) = remember {
        mutableStateOf<Int?>(
            selectedColor
        )
    }
    var colorList by remember {
        mutableStateOf(
            loadDefaultColors(
                context, initiallySelectedCustomColor, setInitiallySelectedCustomColor
            )
        )
    }
    PromptBottomSheetTemplate(
        onDismissRequest = {
           onDismiss(colorData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, colorData))
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_prompts_cancel),
            action = {
                onDismiss(colorData)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, colorData))
            }),
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_prompts_set_date),
            action = {
                onPositiveAction(colorData, selectedColor.toHexColor())
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, colorData))
            })
    ) {
        // title
        InfernoText(
            text = stringResource(R.string.mozac_feature_prompts_choose_a_color) + ":",
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        // color list
        LazyColumn {
            items(colorList) {
                val newColor = it
                ColorListItem(newColor, newColor.color == selectedColor) {
                    // set selected
                    selectedColor = newColor.color
                    // if not in list add
                    val colorItems = colorList.toMutableList()
                    val index = colorItems.indexOfFirst { color -> color.color == newColor.color }
                    val lastColor = if (index > -1) {
                        colorItems[index] = colorItems[index].copy(selected = true)
                        initiallySelectedCustomColor
                    } else {
                        newColor.color
                    }
                    if (lastColor != null) {
                        colorItems.add(lastColor.toColorItem(selected = lastColor == newColor.color))
                    }
                    // update list
                    colorList = colorItems
                }
            }
        }
    }
}

@Composable
private fun ColorListItem(colorItem: ColorItem, selected: Boolean, onColorChange: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .clickable {
                onColorChange.invoke()
            }, verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected) { }
        InfernoText(
            text = colorItem.color.toHexColor(),
            modifier = Modifier.weight(1F),
            textAlign = TextAlign.Left
        )
    }
}

private fun loadDefaultColors(
    context: Context,
    initiallySelectedCustomColor: Int?,
    setInitiallySelectedCustomColor: (Int?) -> Unit
): List<ColorItem> {
    // Load list of colors from resources
    val typedArray =
        context.resources.obtainTypedArray(R.array.mozac_feature_prompts_default_colors)

    val defaultColors = List(typedArray.length()) { i ->
        val color = typedArray.getColor(i, Color.BLACK)
        if (color == initiallySelectedCustomColor) {
            // No need to save the initial color, its already in the list
            setInitiallySelectedCustomColor(null)
        }

        color.toColorItem()
    }
    typedArray.recycle()
    return defaultColors
}