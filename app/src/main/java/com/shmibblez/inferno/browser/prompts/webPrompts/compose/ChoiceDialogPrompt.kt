package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.Choice
import mozilla.components.concept.engine.prompt.PromptRequest
import java.security.InvalidParameterException

internal const val SINGLE_CHOICE_DIALOG_TYPE = 0
internal const val MULTIPLE_CHOICE_DIALOG_TYPE = 1
internal const val MENU_CHOICE_DIALOG_TYPE = 2

internal const val TYPE_MULTIPLE = 1
internal const val TYPE_SINGLE = 2
internal const val TYPE_GROUP = 3
internal const val TYPE_MENU = 4
internal const val TYPE_MENU_SEPARATOR = 5

fun choices(menuData: PromptRequest): Array<Choice> {
    return when (menuData) {
        is PromptRequest.SingleChoice -> menuData.choices
        is PromptRequest.MultipleChoice -> menuData.choices
        is PromptRequest.MenuChoice -> menuData.choices

        else -> {
            throw IllegalArgumentException("unknown class received")
        }
    }
}

@Composable
fun ChoiceDialogPrompt(
    promptRequest: PromptRequest,
    dialogType: Int,
    onCancel: () -> Unit,
    onConfirm: (Any) -> Unit,
) {
    if (promptRequest !is PromptRequest.SingleChoice && promptRequest !is PromptRequest.MultipleChoice && promptRequest !is PromptRequest.MenuChoice) {
        throw InvalidParameterException("menuData must be of types listed here")
    }
    var negativeAction: PromptBottomSheetTemplateAction? = null
    var positiveAction: PromptBottomSheetTemplateAction? = null
    val mapSelectChoice = remember { HashMap<Choice, Choice>() }

    when (dialogType) {
        SINGLE_CHOICE_DIALOG_TYPE, MENU_CHOICE_DIALOG_TYPE -> {
            negativeAction = PromptBottomSheetTemplateAction(
                text = stringResource(android.R.string.cancel),
                action = onCancel,
            )
            // single choice dialog options, dismiss with single option
            positiveAction = PromptBottomSheetTemplateAction(
                text = stringResource(android.R.string.ok),
                action = onCancel,
            )
        }

        MULTIPLE_CHOICE_DIALOG_TYPE -> {
            // multiple choice dialog options
            negativeAction = PromptBottomSheetTemplateAction(
                text = stringResource(android.R.string.cancel),
                action = onCancel,
            )
            positiveAction = PromptBottomSheetTemplateAction(
                text = stringResource(android.R.string.ok),
                action = {
                    onConfirm(mapSelectChoice)
                },
            )
        }
    }
    PromptBottomSheetTemplate(
        onDismissRequest = onCancel,
        negativeAction = negativeAction, positiveAction = positiveAction,
        buttonPosition = PromptBottomSheetTemplateButtonPosition.TOP,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            items(choices(promptRequest)) { choice ->
                val type = deduceItemType(choice, dialogType)
                when (type) {
                    TYPE_MULTIPLE -> {
                        MultipleItem(
                            choice = choice,
                            mapSelectChoice = mapSelectChoice,
                            onConfirm = onConfirm,
                        )
                    }

                    TYPE_SINGLE -> {
                        SingleItem(
                            choice = choice,
                            mapSelectChoice = mapSelectChoice,
                            onConfirm = onConfirm,
                        )
                    }

                    TYPE_GROUP -> {
                        GroupItem(
                            choice = choice,
                        )
                    }

                    TYPE_MENU -> {
                        MenuItem(
                            choice = choice,
                            onConfirm = onConfirm,
                        )
                    }

                    TYPE_MENU_SEPARATOR -> {
                        MenuSeparatorItem()
                    }
                }
            }
        }
    }
}

private fun deduceItemType(choice: Choice, dialogType: Int): Int {
    val isSingleChoice = { dialogType == SINGLE_CHOICE_DIALOG_TYPE }
    val isMenuChoice = { dialogType == MENU_CHOICE_DIALOG_TYPE }

    return when {
        isMenuChoice() -> if (choice.isASeparator) TYPE_MENU_SEPARATOR else TYPE_MENU
        isSingleChoice() and choice.isGroupType -> TYPE_GROUP
        isSingleChoice() -> TYPE_SINGLE
        choice.isGroupType -> TYPE_GROUP
        else -> TYPE_MULTIPLE
    }
}

@Composable
fun MultipleItem(
    choice: Choice,
    mapSelectChoice: HashMap<Choice, Choice>,
    onConfirm: (Array<Choice>) -> Unit,
) {
    var checked by remember { mutableStateOf(choice.selected) }
    Row(modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clickable(enabled = choice.enable) {
            var isChecked: Boolean
            with(mapSelectChoice) {
                if (choice in this) {
                    this -= choice
                    isChecked = false
                } else {
                    this[choice] = choice
                    isChecked = true
                }
            }
            // toggle
            checked = isChecked
            // on select
            onConfirm.invoke(mapSelectChoice.keys.toTypedArray())
        }) {
        InfernoText(
            text = choice.label,
            modifier = Modifier.weight(1F),
        )
        InfernoCheckbox(
            checked = checked,
            enabled = choice.enable,
            onCheckedChange = {},
        )
    }
}

@Composable
fun SingleItem(
    choice: Choice,
    // todo: why this not used
    mapSelectChoice: HashMap<Choice, Choice>,
    onConfirm: (Choice) -> Unit,
) {
    var checked by remember { mutableStateOf(choice.selected) }
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .clickable(enabled = choice.enable) {
                // toggle
                checked = !checked
                // on select
                onConfirm.invoke(choice)
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InfernoText(
            text = choice.label,
            modifier = Modifier.weight(1F),
        )
        InfernoCheckbox(
            checked = checked,
            enabled = choice.enable,
            onCheckedChange = {},
        )
    }
}

@Composable
fun GroupItem(choice: Choice) {
    InfernoText(
        text = choice.label,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
    )
}

@Composable
fun MenuItem(choice: Choice, onConfirm: (Choice) -> Unit) {
    InfernoText(
        text = choice.label,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable(enabled = choice.enable) {
                // on select
                onConfirm.invoke(choice)
            },
    )
}

@Composable
fun MenuSeparatorItem() {
    HorizontalDivider(
        thickness = 1.dp,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}