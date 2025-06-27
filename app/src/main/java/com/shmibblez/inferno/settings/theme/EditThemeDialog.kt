package com.shmibblez.inferno.settings.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoSwitch
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.toolbar.ToToolbarIcon

internal val COLOR_BOX_SIZE = 32.dp

private val LIST_BOTTOM_PADDING = 54.dp

@Composable
fun EditThemeDialog(
    baseTheme: InfernoTheme,
    onDismiss: () -> Unit,
    onSaveTheme: (InfernoTheme, oldThemeName: String) -> Unit,
    themeNames: List<String>,
) {
    // theme being edited, copy of current theme
    var theme by remember { mutableStateOf(baseTheme) }
    val oldThemeName by remember { mutableStateOf(baseTheme.name) }
    val lazyListState = rememberLazyListState()

    var nameError by remember { mutableStateOf(false) }
    fun checkNameError() {
        nameError = themeNames.contains(theme.name)
    }

    var showColorEditorDialogFor by remember { mutableStateOf<ColorEditorDialogData?>(null) }

    // component colors
    val outlinedButtonColors =
        ButtonColors(
            containerColor = theme.primaryBackgroundColor,
            contentColor = theme.primaryOutlineColor,
            disabledContainerColor = theme.secondaryBackgroundColor,
            disabledContentColor = theme.secondaryOutlineColor,
        )
    val checkboxColors =
        CheckboxColors(
            checkedCheckmarkColor = theme.primaryIconColor,
            uncheckedCheckmarkColor = Color.Transparent,
            checkedBoxColor = theme.primaryActionColor,
            uncheckedBoxColor = Color.Transparent,
            disabledCheckedBoxColor = theme.secondaryBackgroundColor,
            disabledUncheckedBoxColor = Color.Transparent,
            disabledIndeterminateBoxColor = theme.secondaryBackgroundColor,
            checkedBorderColor = theme.primaryActionColor,
            uncheckedBorderColor = theme.primaryIconColor,
            disabledBorderColor = theme.secondaryBackgroundColor,
            disabledUncheckedBorderColor = theme.secondaryBackgroundColor,
            disabledIndeterminateBorderColor = theme.secondaryBackgroundColor,
        )
    val switchColors =
        SwitchColors(
            checkedThumbColor = theme.primaryActionColor,
            checkedTrackColor = Color.Transparent,
            checkedBorderColor = theme.primaryActionColor,
            checkedIconColor = theme.primaryIconColor,
            uncheckedThumbColor = theme.primaryActionColor,
            uncheckedTrackColor = Color.Transparent,
            uncheckedBorderColor = theme.primaryActionColor,
            uncheckedIconColor = theme.primaryIconColor,
            disabledCheckedThumbColor = theme.secondaryBackgroundColor,    // .secondaryBackgroundColor,
            disabledCheckedTrackColor = Color.Transparent,    // .secondaryBackgroundColor,
            disabledCheckedBorderColor = theme.secondaryBackgroundColor,   // .secondaryBackgroundColor,
            disabledCheckedIconColor = theme.secondaryIconColor,     // .secondaryIconColor,
            disabledUncheckedThumbColor = theme.secondaryBackgroundColor,  // .secondaryBackgroundColor,
            disabledUncheckedTrackColor = Color.Transparent,  // .secondaryBackgroundColor,
            disabledUncheckedBorderColor = theme.secondaryBackgroundColor, // .secondaryBackgroundColor,
            disabledUncheckedIconColor = theme.secondaryIconColor
        )
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = theme.primaryTextColor,
        unfocusedTextColor = theme.secondaryTextColor,
        disabledTextColor = theme.secondaryTextColor,
        errorTextColor = theme.errorColor,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        cursorColor = theme.primaryOutlineColor,
        errorCursorColor = theme.errorColor,
        selectionColors = TextSelectionColors(
            handleColor = theme.primaryOutlineColor,
            backgroundColor = theme.primaryOutlineColor.copy(
                alpha = 0.4F,
            ),
        ),
        focusedBorderColor = theme.primaryActionColor,
        unfocusedBorderColor = theme.primaryOutlineColor,
        disabledBorderColor = theme.secondaryTextColor,
        errorBorderColor = theme.errorColor,
        focusedLeadingIconColor = theme.primaryTextColor,
        unfocusedLeadingIconColor = theme.secondaryTextColor,
        disabledLeadingIconColor = theme.secondaryTextColor,
        errorLeadingIconColor = theme.errorColor,
        focusedTrailingIconColor = theme.primaryTextColor,
        unfocusedTrailingIconColor = theme.secondaryTextColor,
        disabledTrailingIconColor = theme.secondaryTextColor,
        errorTrailingIconColor = theme.errorColor,
        focusedLabelColor = theme.primaryActionColor,
        unfocusedLabelColor = theme.primaryOutlineColor,
        disabledLabelColor = theme.secondaryTextColor,
        errorLabelColor = theme.errorColor,
        focusedPlaceholderColor = theme.secondaryTextColor,
        unfocusedPlaceholderColor = theme.secondaryTextColor,
        disabledPlaceholderColor = theme.secondaryTextColor,
        errorPlaceholderColor = theme.errorColor,
        focusedSupportingTextColor = theme.primaryTextColor,
        unfocusedSupportingTextColor = theme.secondaryTextColor,
        disabledSupportingTextColor = theme.secondaryTextColor,
        errorSupportingTextColor = theme.errorColor,
        focusedPrefixColor = theme.primaryTextColor,
        unfocusedPrefixColor = theme.secondaryTextColor,
        disabledPrefixColor = theme.secondaryTextColor,
        errorPrefixColor = theme.errorColor,
        focusedSuffixColor = theme.primaryTextColor,
        unfocusedSuffixColor = theme.secondaryTextColor,
        disabledSuffixColor = theme.secondaryTextColor,
        errorSuffixColor = theme.errorColor,
    )

    InfernoDialog(
        onDismiss = onDismiss,
        colors = CardColors(
            containerColor = theme.primaryBackgroundColor,
            contentColor = theme.primaryTextColor,
            disabledContainerColor = theme.secondaryBackgroundColor,
            disabledContentColor = theme.secondaryBackgroundColor,
        ),
        border = BorderStroke(1.dp, theme.primaryActionColor),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING),
                verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING * 2),
            ) {
                /**
                 * name
                 */

                item {
                    InfernoOutlinedTextField(
                        value = theme.name,
                        onValueChange = {
                            val name = it.let { s ->
                                when {
                                    s.length > PrefUiConst.CUSTOM_THEME_MAX_NAME_LENGTH -> s.substring(
                                        0, PrefUiConst.CUSTOM_THEME_MAX_NAME_LENGTH
                                    )

                                    else -> s
                                }
                            }
                            theme = theme.copy(name = name)
                            checkNameError()
                        },
                        label = {
                            InfernoText(
                                text = "Theme Name", // todo: string res
                                fontColor = LocalContext.current.infernoTheme().value.primaryOutlineColor
                            )
                        }, // todo: string res
                        isError = nameError,
                        supportingText = {
                            if (nameError) {
                                InfernoText(
                                    text = "Theme name taken, it will be overridden.", // todo: string res
                                    infernoStyle = InfernoTextStyle.Error,
                                )
                            }
                        },
                        colors = textFieldColors,
                    )
                }

                /**
                 * text color
                 */

                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "primary text color:", // todo: string res
                            color = theme.primaryTextColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(primaryTextColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(primaryTextColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(primaryTextColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "secondary text color:", // todo: string res
                            color = theme.secondaryTextColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(secondaryTextColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(secondaryTextColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(secondaryTextColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    val funFacts = remember { funFactSelector(2) }
                    ColorPreviewColumn(theme = theme) {
                        InfernoText(
                            text = funFacts[0], infernoStyle = InfernoTextStyle.Normal,
                            fontColor = theme.primaryTextColor,
                        )
                        InfernoText(
                            text = funFacts[1], infernoStyle = InfernoTextStyle.SmallSecondary,
                            fontColor = theme.secondaryTextColor,
                        )
                    }
                }

                /**
                 * icon color
                 */

                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "primary icon color:", // todo: string res
                            color = theme.primaryIconColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(primaryIconColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(primaryIconColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(primaryIconColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "secondary icon color:", // todo: string res
                            color = theme.secondaryIconColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(secondaryIconColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(secondaryIconColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(secondaryIconColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    ColorPreviewRow(theme = theme) {
                        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK.ToToolbarIcon(tint = theme.secondaryIconColor)
                        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD.ToToolbarIcon(tint = theme.secondaryIconColor)
                        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD.ToToolbarIcon(tint = theme.primaryIconColor)
                        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI.ToToolbarIcon(tint = theme.primaryIconColor)
                    }
                }

                /**
                 * outline color
                 */

                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "primary outline color:", // todo: string res
                            color = theme.primaryOutlineColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(primaryOutlineColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(primaryOutlineColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(primaryOutlineColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "secondary outline color:", // todo: string res
                            color = theme.secondaryOutlineColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(secondaryOutlineColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(secondaryOutlineColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(secondaryOutlineColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    val funFacts = remember { funFactSelector(2) }
                    ColorPreviewColumn(theme = theme) {
                        InfernoOutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedButtonColors,
                        ) {
                            InfernoText(
                                text = funFacts[0],
                                fontColor = theme.primaryOutlineColor,
                                maxLines = 1,
                            )
                        }
                        InfernoOutlinedTextField(
                            value = funFacts[1],
                            onValueChange = {},
                            readOnly = true,
                            maxLines = 1,
                            colors = textFieldColors,
                        )
                    }
                }

                /**
                 * action color
                 */

                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "primary action color:", // todo: string res
                            color = theme.primaryActionColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(primaryActionColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(primaryActionColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(primaryActionColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "secondary action color:", // todo: string res
                            color = theme.secondaryActionColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(secondaryActionColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(secondaryActionColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(secondaryActionColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    ColorPreviewRow(theme = theme) {
                        InfernoCheckbox(
                            checked = true,
                            onCheckedChange = {},
                            colors = checkboxColors,
                        )
                        InfernoCheckbox(
                            checked = false,
                            onCheckedChange = {},
                            colors = checkboxColors,
                        )
                        InfernoSwitch(
                            checked = false,
                            onCheckedChange = {},
                            colors = switchColors,
                        )
                        InfernoSwitch(
                            checked = true,
                            onCheckedChange = {},
                            colors = switchColors,
                        )
                    }
                }

                /**
                 * error color
                 */

                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "error color:", // todo: string res
                            color = theme.errorColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(errorColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(errorColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(errorColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    val funFacts = remember { funFactSelector(2) }
                    ColorPreviewColumn(theme = theme) {
                        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                            InfernoText(
                                text = funFacts[0],
                                fontColor = theme.primaryOutlineColor,
                                maxLines = 1,
                            )
                        }
                        InfernoOutlinedTextField(
                            value = funFacts[1],
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                InfernoIcon(
                                    painter = painterResource(R.drawable.mozac_ic_warning_fill_24),
                                    contentDescription = "",
                                    tint = theme.errorColor,
                                )
                            },
                            isError = true,
                            supportingText = {
                                InfernoText(
                                    text = stringResource(android.R.string.httpErrorBadUrl),
                                    infernoStyle = InfernoTextStyle.Error,
                                )
                            },
                            maxLines = 1,
                            colors = textFieldColors,
                        )
                    }
                }

                /**
                 * background color
                 */

                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "primary background color:", // todo: string res
                            color = theme.primaryBackgroundColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(primaryBackgroundColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(primaryBackgroundColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(primaryBackgroundColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    ColorEditor(
                        data = ColorEditorDialogData(
                            name = "secondary background color:", // todo: string res
                            color = theme.secondaryBackgroundColor,
                            theme = theme,
                            onDismiss = {
                                theme = theme.copy(secondaryBackgroundColor = it)
                                showColorEditorDialogFor = null
                            },
                            onConfirm = {
                                theme = theme.copy(secondaryBackgroundColor = it)
                                showColorEditorDialogFor = null
                            },
                            onChange = { theme = theme.copy(secondaryBackgroundColor = it) },
                        ),
                        setShowColorEditorDialogFor = { showColorEditorDialogFor = it },
                    )
                }
                item {
                    ColorPreviewColumn(previewText = "Preview (Background)", theme = theme) {
                        val funFact = funFactSelector(1)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(theme.secondaryBackgroundColor)
                                .padding(16.dp),
                        ) {
                            InfernoText(funFact[0], fontColor = theme.primaryTextColor)
                        }
                    }
                }
                // bottom padding
                item {
                    Spacer(Modifier.height(LIST_BOTTOM_PADDING))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InfernoOutlinedButton(
                    text = stringResource(android.R.string.cancel),
                    modifier = Modifier.weight(1F),
                    onClick = onDismiss,
                    border = BorderStroke(
                        width = 1.dp,
                        color = theme.primaryOutlineColor,
                    ),
                    colors = outlinedButtonColors,
                )
                InfernoButton(
                    text = stringResource(R.string.browser_menu_save),
                    modifier = Modifier.weight(1F),
                    onClick = { onSaveTheme.invoke(theme, oldThemeName) },
                    colors = ButtonColors(
                        containerColor = theme.primaryActionColor,
                        contentColor = theme.primaryTextColor,
                        disabledContainerColor = theme.secondaryActionColor,
                        disabledContentColor = theme.secondaryTextColor,
                    ),
                )
            }
        }
        if (showColorEditorDialogFor != null) {
            ColorEditorDialog(data = showColorEditorDialogFor!!)
        }
    }
}

@Composable
private fun ColorEditor(
    data: ColorEditorDialogData,
    setShowColorEditorDialogFor: (ColorEditorDialogData) -> Unit,
) {
    // color box, name, and edit button
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING, Alignment.Start
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // color name
        InfernoText(text = data.name, modifier = Modifier.weight(1F))

        // color preview box
        Box(
            modifier = Modifier
                .background(data.color, MaterialTheme.shapes.small)
                .border(1.dp, data.theme.primaryOutlineColor, MaterialTheme.shapes.small)
                .size(COLOR_BOX_SIZE),
        )

        // edit button
        InfernoText(
            text = stringResource(R.string.browser_menu_edit),
            fontColor = data.theme.primaryActionColor,
            modifier = Modifier.clickable {
                setShowColorEditorDialogFor(data)
            },
        )
    }
}

@Composable
private fun ColorPreviewRow(
    previewText: String = "Preview", // todo: string res
    theme: InfernoTheme,
    content: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        PreviewHorizontalDivider(theme)
        InfernoText(previewText, fontColor = theme.primaryTextColor)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content.invoke(this)
        }
        PreviewHorizontalDivider(theme)
    }
}

@Composable
private fun PreviewHorizontalDivider(theme: InfernoTheme) {
    HorizontalDivider(thickness = 1.dp, color = theme.primaryIconColor)
}

@Composable
private fun ColorPreviewColumn(
    previewText: String = "Preview", // todo: string res
    theme: InfernoTheme,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PreviewHorizontalDivider(theme)
        InfernoText(previewText, fontColor = theme.primaryTextColor)
        content.invoke(this)
        PreviewHorizontalDivider(theme)
    }
}

/**
 * @param n: number of fun facts to return, max 20
 */
private fun funFactSelector(n: Int): List<String> {
    // todo: string res
    val funFacts = listOf(
        "Sharks existed before trees and have barely changed since.",
        "There’s a species of jellyfish that can essentially live forever.",
        "You can fit all the planets of the solar system between Earth and the Moon.",
        "Bananas are radioactive enough to trigger airport radiation sensors.",
        "Octopuses have three hearts and they stop beating when they swim.",
        "Your bones are constantly being replaced and you're basically a walking Ship of Theseus.",
        "Space smells like seared steak according to astronauts.",
        "If the sun were the size of a white blood cell, the Milky Way would be the size of the U.S.",
        "There are more fake flamingos in the world than real ones.",
        "Humans glow in the dark—we’re just too blind to see it.",
        "Wombat poop is cube-shaped and scientists still aren’t fully sure why.",
        "You started as a single cell that split into 37 trillion cells with zero meetings.",
        "Time passes slightly faster on your head than on your feet.",
        "Sloths can hold their breath longer than dolphins.",
        "Cleopatra lived closer to the Moon landing than to the building of the pyramids.",
        "Crows can remember human faces—and hold grudges.",
        "You technically have more bacteria DNA in your body than human DNA.",
        "The brain named itself and is annoyed you’re thinking about it now.",
        "There's a planet made entirely of diamonds out there, but you're stuck paying rent.",
        "The word ‘robot’ comes from a Czech word meaning ‘forced labor’—how fitting."
    )
    return funFacts.shuffled().subList(0, n)
}
