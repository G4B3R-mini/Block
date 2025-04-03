package com.shmibblez.inferno.browser.prompts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.base.InfernoText


enum class PromptBottomSheetTemplateButtonPosition {
    BOTTOM, TOP, NONE,
}

data class PromptBottomSheetTemplateAction(
    val text: String, val action: () -> Unit, val enabled: Boolean = true,
)


/**
 * @param onDismissRequest what to do when dialog dismissed
 * @param dismissOnSwipeDown whether should dismiss on swipe down
 * @param negativeAction negative action text and function
 * @param positiveAction positive action text and function
 * @param content dialog content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptBottomSheetTemplate(
    onDismissRequest: (() -> Unit)?,
    dismissOnSwipeDown: Boolean = onDismissRequest != null,
    negativeAction: PromptBottomSheetTemplateAction? = null,
    neutralAction: PromptBottomSheetTemplateAction? = null,
    positiveAction: PromptBottomSheetTemplateAction? = null,
    buttonPosition: PromptBottomSheetTemplateButtonPosition = PromptBottomSheetTemplateButtonPosition.TOP,
    content: @Composable (ColumnScope.() -> Unit),
) {
    // don't dismiss on swipe down
    val sheetState = rememberModalBottomSheetState(confirmValueChange = { sheetValue ->
        sheetValue != SheetValue.Hidden || dismissOnSwipeDown
    })
    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest ?: {},
        // todo: use acorn colors
        containerColor = Color.Black,
        scrimColor = Color.Black.copy(alpha = 0.5F),
        shape = RectangleShape,
        dragHandle = { /* no drag handle */ },
        content = {
            // content actions (cancel or confirm)
            // content below
            if (buttonPosition == PromptBottomSheetTemplateButtonPosition.BOTTOM && (positiveAction != null || negativeAction != null)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                )
            }
            if (buttonPosition == PromptBottomSheetTemplateButtonPosition.TOP && (positiveAction != null || neutralAction != null || negativeAction != null)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // todo: button text color and colors if not enabled
                        if (negativeAction != null) {
//                            TextButton(
//                                onClick = negativeAction.action,
//                                enabled = negativeAction.enabled,
//                            ) {
                            InfernoText(
                                text = negativeAction.text,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .weight(1F)
                                    .clickable(
                                        onClick = negativeAction.action,
                                        enabled = negativeAction.enabled,
                                    ),
                                fontWeight = FontWeight.Bold,
                                fontColor = Color(143, 0, 255),
                            )
//                            }
                        }
                        if (neutralAction != null) {
//                            TextButton(
//                                onClick = neutralAction.action,
//                                enabled = neutralAction.enabled,
//                            ) {
                            InfernoText(
                                text = neutralAction.text,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .weight(1F)
                                    .clickable(
                                        onClick = neutralAction.action,
                                        enabled = neutralAction.enabled,
                                    ),
                                fontWeight = FontWeight.Bold,
                                fontColor = Color(143, 0, 255),
                            )
//                            }
                        }
                        if (positiveAction != null) {
//                            TextButton(
//                                onClick = positiveAction.action,
//                                enabled = positiveAction.enabled,
//                            ) {
                            InfernoText(
                                text = positiveAction.text,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .weight(1F)
                                    .clickable(
                                        onClick = positiveAction.action,
                                        enabled = positiveAction.enabled,
                                    ),
                                fontWeight = FontWeight.Bold,
                                fontColor = Color(143, 0, 255),
                            )
//                            }
                        }
                    }
                    HorizontalDivider(
                        color = Color.LightGray, modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    )
                }
            }
            // content
            content.invoke(this)
            // bottom buttons
            if (buttonPosition == PromptBottomSheetTemplateButtonPosition.BOTTOM && (positiveAction != null || neutralAction != null || negativeAction != null)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // todo: button text color and colors if not enabled
                    if (negativeAction != null) {
                        OutlinedButton(
                            onClick = negativeAction.action,
                            modifier = Modifier
                                .weight(1F)
                                .fillMaxHeight(),
                            enabled = negativeAction.enabled,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(width = 1.dp, color = Color.White),
                        ) {
                            InfernoText(
                                text = negativeAction.text,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(all = 4.dp)
                            )
                        }
                    }

                    if (neutralAction != null) {
                        OutlinedButton(
                            onClick = neutralAction.action,
                            modifier = Modifier
                                .weight(1F)
                                .fillMaxHeight(),
                            enabled = neutralAction.enabled,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(width = 1.dp, color = Color.White),
                        ) {
                            InfernoText(
                                text = neutralAction.text,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(all = 4.dp)
                            )
                        }
                    }

                    if (positiveAction != null) {
                        Button(
                            onClick = positiveAction.action,
                            modifier = Modifier
                                .weight(1F)
                                .fillMaxHeight(),
                            enabled = positiveAction.enabled,
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(143, 0, 255)
                            ),
                        ) {
                            InfernoText(
                                text = positiveAction.text,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(all = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
            )
        },
    )
}