package com.shmibblez.inferno.compose.base

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.ext.infernoTheme

// todo: replace all usages of Icon with Inferno Icon
//  and remove all instances of tint = Color.White or iconPrimary

@Composable
fun InfernoIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = when (enabled) {
        true -> LocalContext.current.infernoTheme().value.primaryIconColor
        false -> LocalContext.current.infernoTheme().value.secondaryIconColor
    },
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}