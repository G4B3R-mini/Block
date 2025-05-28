package com.shmibblez.inferno.settings.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import kotlinx.coroutines.launch
import mozilla.components.support.ktx.kotlin.toHexString

// for removing invalid characters
private val ARGB_STRING_FILTER = Regex("[^a-zA-Z0-9]")

// regex for rgb strings
private val RGB_STRING_REGEX = Regex("^[a-zA-Z0-9]{6}\$")

// regex for argb strings
private val ARGB_STRING_REGEX = Regex("^[a-zA-Z0-9]{8}\$")

private val COLOR_BOX_SIZE = 32.dp

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
        focusedBorderColor = theme.secondaryOutlineColor,
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
        focusedLabelColor = theme.secondaryOutlineColor,
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
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1F),
                verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
            ) {
                /**
                 * name
                 */
                item {
                    InfernoOutlinedTextField(
                        value = theme.name,
                        onValueChange = {
                            theme.name = it.substring(
                                0, PrefUiConst.CUSTOM_THEME_MAX_NAME_LENGTH
                            )
                        },
                        label = {
                            InfernoText(
                                text = "Theme Name",
                                fontColor = LocalContext.current.infernoTheme().value.primaryOutlineColor
                            )
                        }, // todo: string res
                        isError = themeNames.contains(theme.name),
                        supportingText = {
                            InfernoText(
                                text = "Theme name taken, it will be overridden.", // todo: string res
                                infernoStyle = InfernoTextStyle.Error,
                            )
                        },
                    )
                }
                /**
                 * text color
                 */
                item {
                    ColorEditor(
                        name = "primary text color:", // todo: string res
                        color = theme.primaryTextColor,
                        onChangeColor = { theme = theme.copy(primaryTextColor = it) },
                        theme = theme,
                    )
                }
                item {
                    ColorEditor(
                        name = "secondary text color:", // todo: string res
                        color = theme.secondaryTextColor,
                        onChangeColor = { theme = theme.copy(secondaryTextColor = it) },
                        theme = theme,
                    )
                }
                item {
                    val funFacts = remember { funFactSelector(2) }
                    ColorPreviewColumn {
                        InfernoText(
                            text = funFacts[0], infernoStyle = InfernoTextStyle.Normal,
                            fontColor = theme.primaryTextColor,
                        )
                        InfernoText(
                            text = funFacts[0], infernoStyle = InfernoTextStyle.Subtitle,
                            fontColor = theme.secondaryTextColor,
                        )
                    }
                }
                /**
                 * icon color
                 */
                item {
                    ColorEditor(
                        name = "primary icon color:", // todo: string res
                        color = theme.primaryIconColor,
                        onChangeColor = { theme = theme.copy(primaryIconColor = it) },
                        theme = theme,
                    )
                }
                item {
                    ColorEditor(
                        name = "secondary icon color:", // todo: string res
                        color = theme.secondaryIconColor,
                        onChangeColor = { theme = theme.copy(secondaryIconColor = it) },
                        theme = theme,
                    )
                }
                item {
                    ColorPreviewRow {
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
                        name = "primary outline color:", // todo: string res
                        color = theme.primaryOutlineColor,
                        onChangeColor = { theme = theme.copy(primaryOutlineColor = it) },
                        theme = theme,
                    )
                }
                item {
                    ColorEditor(
                        name = "primary outline color:", // todo: string res
                        color = theme.secondaryOutlineColor,
                        onChangeColor = { theme = theme.copy(secondaryOutlineColor = it) },
                        theme = theme,
                    )
                }
                item {
                    val funFacts = remember { funFactSelector(2) }
                    ColorPreviewColumn {
                        InfernoOutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors().copy(
                                containerColor = Color.Transparent,
                                contentColor = theme.primaryOutlineColor,
                                disabledContainerColor = Color.Transparent,
                            )
                        ) {
                            InfernoText(
                                text = funFacts[0], fontColor = theme.primaryOutlineColor
                            )
                        }
                        InfernoOutlinedTextField(
                            value = funFacts[1],
                            onValueChange = {},
                            readOnly = true,
                            colors = textFieldColors,
                        )
                    }
                }
                /**
                 * action color
                 */
                item {
                    ColorEditor(
                        name = "primary action color:", // todo: string res
                        color = theme.primaryActionColor,
                        onChangeColor = { theme = theme.copy(primaryActionColor = it) },
                        theme = theme,
                    )
                }
                item {
                    ColorEditor(
                        name = "secondary action color:", // todo: string res
                        color = theme.secondaryActionColor,
                        onChangeColor = { theme = theme.copy(secondaryActionColor = it) },
                        theme = theme,
                    )
                }
                item {
                    ColorPreviewRow {
                        InfernoCheckbox(
                            checked = true,
                            onCheckedChange = {},
                            colors = CheckboxColors(
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
                            ),
                        )
                        InfernoCheckbox(
                            checked = false,
                            onCheckedChange = {},
                            colors = CheckboxColors(
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
                            ),
                        )
                        InfernoSwitch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchColors(
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
                            ),
                        )
                        InfernoSwitch(
                            checked = false,
                            onCheckedChange = {},
                            colors = SwitchColors(
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
                            ),
                        )
                    }
                }
                /**
                 * error color
                 */
                item {
                    ColorEditor(
                        name = "error color:", // todo: string res
                        color = theme.errorColor,
                        onChangeColor = { theme = theme.copy(errorColor = it) },
                        theme = theme,
                    )
                }
                item {
                    val funFacts = remember { funFactSelector(2) }
                    ColorPreviewColumn(theme.secondaryBackgroundColor) {
                        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                            InfernoText(
                                text = funFacts[0], fontColor = theme.primaryOutlineColor
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
                            colors = OutlinedTextFieldDefaults.colors(
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
                                focusedBorderColor = theme.secondaryOutlineColor,
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
                                focusedLabelColor = theme.secondaryOutlineColor,
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
                            ),
                        )
                    }
                }
                /**
                 * background color
                 */
                item {
                    ColorEditor(
                        name = "primary background color:", // todo: string res
                        color = theme.primaryBackgroundColor,
                        onChangeColor = { theme = theme.copy(primaryBackgroundColor = it) },
                        theme = theme,
                    )
                }
                item {
                    ColorEditor(
                        name = "secondary background color:", // todo: string res
                        color = theme.secondaryBackgroundColor,
                        onChangeColor = { theme = theme.copy(secondaryBackgroundColor = it) },
                        theme = theme,
                    )
                }
                item {
                    ColorPreviewColumn {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InfernoOutlinedButton(
                    text = stringResource(android.R.string.cancel),
                    modifier = Modifier.weight(1F), onClick = onDismiss,
                    border = BorderStroke(
                        width = 1.dp,
                        color = theme.primaryOutlineColor,
                    ),
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
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
private fun ColorEditor(
    name: String,
    color: Color,
    onChangeColor: (Color) -> Unit,
    theme: InfernoTheme,
) {
    val coroutineScope = rememberCoroutineScope()
    var input by remember { mutableStateOf(color.toArgbHexString()) }
    val tooltipState = rememberTooltipState(isPersistent = true)

    // true if color is badly formatted
    var colorBad by remember { mutableStateOf(false) }

    fun checkForErrors() {
        colorBad = !RGB_STRING_REGEX.matches(input) && !ARGB_STRING_REGEX.matches(input)
    }

    // tooltip wrapper
    TooltipBox(
        modifier = Modifier.fillMaxWidth(),
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            InfernoText(
                // todo: string res
                text = "Hex colors must be in format #RRGGBB or #AARRGGBB." +
                        // alpha
                        "\n- AA is alpha component and takes values from 00 (transparent) to FF (fully opaque)" +
                        // red
                        "\n- RR is red component and takes values from 00 to FF" +
                        // green
                        "\n- GG is green component and takes values from 00 to FF" +
                        // blue
                        "\n- BB is blue component and takes values from 00 to FF" +
                        // examples
                        "\nSome examples:" +
                        // red
                        "\n- Solid red would be #FF0000 (#FFFF0000 with alpha)" +
                        // green
                        "\n- Solid green would be #00FF00 (#FF00FF00 with alpha)" +
                        // blue
                        "\n- Solid blue would be #0000FF (#FF0000FF with alpha)" +
                        // purple
                        "\n- You can also make colors transparent, in this case half transparent purple would be #889000FF",
                infernoStyle = InfernoTextStyle.Subtitle,
            )
        },
        state = tooltipState,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // color name
            InfernoText(text = name, modifier = Modifier.weight(3F))


            // color hex editor
            InfernoOutlinedTextField(
                modifier = Modifier.weight(1F),
                value = input,
                onValueChange = {
                    input = ARGB_STRING_FILTER.replace(it, "")
                    // if string is valid hex, update color
                    when {
                        RGB_STRING_REGEX.matches(it) -> onChangeColor(Color(("FF$it").hexToInt()))
                        ARGB_STRING_REGEX.matches(it) -> onChangeColor(Color(it.hexToInt()))
                    }
                },
                leadingIcon = {
                    InfernoText("#")
                },
                supportingText = {
                    if (colorBad) {
                        InfernoText(
                            text = "Must have either 6 or 8 characters. Learn more.", // todo: string res
                            infernoStyle = InfernoTextStyle.Error,
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    tooltipState.show()
                                }
                            },
                        )
                    }
                },
                isError = colorBad,
            )

            // color preview box
            Box(
                modifier = Modifier
                    .background(color, MaterialTheme.shapes.small)
                    .border(1.dp, theme.primaryOutlineColor, MaterialTheme.shapes.small)
                    .size(COLOR_BOX_SIZE),
            )
        }
    }
}

fun Color.toArgbHexString(): String {
    return this.toArgb().argbIntToHexString()
}

fun Int.argbIntToHexString(): String {
    val a = (this shr 24).toByte()
    val r = (this shr 16).toByte()
    val g = (this shr 8).toByte()
    val b = (this).toByte()
    return byteArrayOf(a, r, g, b).toHexString()
}

@Composable
private fun ColorPreviewRow(
    background: Color = Color.Transparent,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content.invoke(this)
    }
}

@Composable
private fun ColorPreviewColumn(
    background: Color = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content.invoke(this)
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
