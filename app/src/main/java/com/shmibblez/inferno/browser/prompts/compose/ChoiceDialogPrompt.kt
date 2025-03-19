package com.shmibblez.inferno.browser.prompts.compose

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
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onNegativeAction
import com.shmibblez.inferno.browser.prompts.onPositiveAction
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
fun <T> ChoiceDialogPrompt(
    menuData: T, sessionId: String, dialogType: Int
) where T : PromptRequest {
    if (menuData !is PromptRequest.SingleChoice && menuData !is PromptRequest.MultipleChoice && menuData !is PromptRequest.MenuChoice) {
        throw InvalidParameterException("menuData must be of types listed here")
    }
    val store = LocalContext.current.components.core.store
    var negativeAction: PromptBottomSheetTemplateAction? = null
    var positiveAction: PromptBottomSheetTemplateAction? = null
    var mapSelectChoice = remember { HashMap<Choice, Choice>() }
    when (dialogType) {
        SINGLE_CHOICE_DIALOG_TYPE, MENU_CHOICE_DIALOG_TYPE -> {
            negativeAction = PromptBottomSheetTemplateAction(
                text = stringResource(android.R.string.cancel),
                action = {
                    // dismiss
                    onNegativeAction(menuData)
                    store.dispatch(
                        ContentAction.ConsumePromptRequestAction(
                            sessionId, menuData
                        )
                    )
                },
            )
            // single choice dialog options, dismiss with single option
            positiveAction = PromptBottomSheetTemplateAction(
                text = stringResource(android.R.string.ok),
                action = {
                    // dismiss
                    onNegativeAction(menuData)
                    store.dispatch(
                        ContentAction.ConsumePromptRequestAction(
                            sessionId, menuData
                        )
                    )
                },
            )
        }

        MULTIPLE_CHOICE_DIALOG_TYPE -> {
            // multiple choice dialog options
            negativeAction =
                PromptBottomSheetTemplateAction(text = stringResource(android.R.string.cancel),
                    action = {
                        // dismiss
                        onDismiss(menuData)
                        store.dispatch(
                            ContentAction.ConsumePromptRequestAction(
                                sessionId, menuData
                            )
                        )
                    })
            positiveAction =
                PromptBottomSheetTemplateAction(text = stringResource(android.R.string.ok),
                    action = {
                        // onConfirm
                        onPositiveAction(menuData, mapSelectChoice)
                        store.dispatch(
                            ContentAction.ConsumePromptRequestAction(
                                sessionId, menuData
                            )
                        )
                    })
        }
    }
    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(menuData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, menuData))
        },
        negativeAction = negativeAction, positiveAction = positiveAction,
        buttonPosition = PromptBottomSheetTemplateButtonPosition.TOP,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            items(choices(menuData)) {
                val type = deduceItemType(it, dialogType)
                when (type) {
                    TYPE_MULTIPLE -> {
                        MultipleItem(
                            choice = it,
                            menuData as PromptRequest.MultipleChoice,
                            sessionId,
                            mapSelectChoice
                        )
                    }

                    TYPE_SINGLE -> {
                        SingleItem(
                            choice = it,
                            menuData as PromptRequest.SingleChoice,
                            sessionId,
                            mapSelectChoice
                        )
                    }

                    TYPE_GROUP -> {
                        GroupItem(choice = it)
                    }

                    TYPE_MENU -> {
                        MenuItem(choice = it, menuData as PromptRequest.MenuChoice, sessionId)
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
    menuData: PromptRequest.MultipleChoice,
    sessionId: String,
    mapSelectChoice: HashMap<Choice, Choice>
) {
    val store = LocalContext.current.components.core.store
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
            onPositiveAction(menuData, mapSelectChoice.keys.toTypedArray())
            // dismiss
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, menuData))
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
    menuData: PromptRequest.SingleChoice,
    sessionId: String,
    // todo: why this not used
    mapSelectChoice: HashMap<Choice, Choice>,
) {
    val store = LocalContext.current.components.core.store
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
                onPositiveAction(menuData, choice)
                // dismiss
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, menuData))
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
fun MenuItem(choice: Choice, menuData: PromptRequest.MenuChoice, sessionId: String) {
    val store = LocalContext.current.components.core.store
    InfernoText(
        text = choice.label,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable(enabled = choice.enable) {
                // on select
                onPositiveAction(menuData, choice)
                // dismiss
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, menuData))
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