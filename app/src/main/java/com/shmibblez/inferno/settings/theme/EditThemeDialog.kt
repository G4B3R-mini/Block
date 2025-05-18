package com.shmibblez.inferno.settings.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.settings.compose.components.PreferenceConstants

@Composable
fun EditThemeDialog(
    baseTheme: InfernoTheme,
    onDismiss: () -> Unit,
    onSaveTheme: (InfernoTheme) -> Unit,
) {
    var theme by remember { mutableStateOf(baseTheme) }


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
        ),
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = theme.secondaryBackgroundColor),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1F)
                ) {
                    item {
                        ColorEditor(
                            color = theme.primaryTextColor,
                            onChangeColor = { theme = theme.copy(primaryIconColor = it) },
                        )
                    }
                    item {
                        ColorEditor(
                            color = theme.secondaryTextColor,
                            onChangeColor = { theme = theme.copy(secondaryTextColor = it) },
                        )
                    }
                    item {
                        val funFacts = remember { funFactSelector(2) }
                        ColorPreview {
                            InfernoText(text = funFacts[0], infernoStyle = InfernoTextStyle.Normal)
                            InfernoText(
                                text = funFacts[0], infernoStyle = InfernoTextStyle.Subtitle
                            )
                        }
                        // todo: add remaining items
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                // todo: add buttons to save/cancel here
            }
        }
    }
}

@Composable
private fun ColorEditor(color: Color, onChangeColor: (Color) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // todo: left off here


        // todo: show pref name, textfield to edit color, and color square at the end
        //  textfield should only allow 0-9, a-f, A-F
        //  show error if color not properly formatted, in error show hint: "Color should be in format #RRGGBBAA
        //  parse hex as color and then call onChangeColor once successful
    }
}

@Composable
private fun ColorPreview(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content.invoke(this)
    }
}

/**
 * @param n: number of fun facts to return, max 20
 */
private fun funFactSelector(n: Int): List<String> {
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
