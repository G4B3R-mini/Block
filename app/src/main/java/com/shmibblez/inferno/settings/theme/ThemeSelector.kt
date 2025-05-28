package com.shmibblez.inferno.settings.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.shmibblez.inferno.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.settings.compose.components.PrefUiConst

@Composable
fun ThemeSelector(
    modifier:Modifier = Modifier,
    selectedDefault: InfernoTheme?,
    selectedCustom: InfernoTheme?,
    defaultThemes: List<InfernoTheme>,
    customThemes: List<InfernoTheme>,
    onSelectTheme: (InfernoTheme) -> Unit,
    onAddTheme: () -> Unit,
    onEditTheme: (InfernoTheme) -> Unit,
    onDeleteTheme: (InfernoTheme) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(
            horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
            vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        header {
            InfernoText(
                text = "Default Themes", // todo: string res
                infernoStyle = InfernoTextStyle.Title
            )
        }
        items(defaultThemes) {
            ThemeItem(
                theme = it,
                selected = selectedDefault?.name == it.name,
                onSelectTheme = onSelectTheme,
                onEditTheme = onEditTheme,
                onDeleteTheme = onDeleteTheme,
            )
        }
        header {
            InfernoText(
                text = "Custom Themes", // todo: string res
                infernoStyle = InfernoTextStyle.Title
            )
        }
        if (customThemes.isNotEmpty()) {
            items(customThemes) {
                ThemeItem(
                    theme = it,
                    selected = selectedCustom?.name == it.name,
                    onSelectTheme = onSelectTheme,
                    onEditTheme = onEditTheme,
                    onDeleteTheme = onDeleteTheme,
                )
            }
        }
        item {
            AddCustomThemeButton(
                onAddTheme = onAddTheme,
                theme = selectedDefault ?: selectedCustom!!,
            )
        }
    }
}

fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit,
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

@Composable
private fun ThemeItem(
    theme: InfernoTheme,
    selected: Boolean,
    onSelectTheme: (InfernoTheme) -> Unit,
    onEditTheme: (InfernoTheme) -> Unit,
    onDeleteTheme: (InfernoTheme) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    if (!selected) onSelectTheme.invoke(theme)
                },
                onLongClick = { expanded = true },
            )
            .fillMaxWidth()
            .background(
                color = when (selected) {
                    true -> theme.primaryBackgroundColor
                    false -> theme.secondaryBackgroundColor
                },
                shape = MaterialTheme.shapes.medium,
            )
            .border(
                width = 2.dp,
                color = when (selected) {
                    true -> theme.secondaryOutlineColor
                    false -> theme.primaryOutlineColor
                },
                shape = MaterialTheme.shapes.medium,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (selected) {
                InfernoText(text = "(${stringResource(R.string.tab_tray_multiselect_selected_content_description)})")
            } else {
                InfernoText(" ")
            }
            ColorSquare(
                color1 = theme.primaryTextColor,
                color2 = theme.primaryActionColor,
                color3 = theme.primaryBackgroundColor,
                color4 = theme.primaryIconColor,
                outlineColor = theme.primaryOutlineColor,
                selected = selected,
            )
            InfernoText(text = theme.name, fontColor = theme.primaryTextColor, maxLines = 1)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = theme.secondaryBackgroundColor,
        ) {
            // edit theme menu item
            DropdownMenuItem(
                text = {
                    InfernoText("Edit theme") // todo: string resource
                },
                onClick = { onEditTheme.invoke(theme) },
                leadingIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_edit_24),
                        contentDescription = "",
                    )
                },
            )
            // delete theme menu item
            DropdownMenuItem(
                text = {
                    InfernoText("Delete theme") // todo: string resource
                },
                onClick = { onDeleteTheme.invoke(theme) },
                leadingIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_delete_24),
                        contentDescription = "",
                    )
                },
            )
        }
    }
}

@Composable
private fun ColorSquare(
    color1: Color,
    color2: Color,
    color3: Color,
    color4: Color,
    outlineColor: Color,
    selected: Boolean,
) {
    Canvas(
        modifier = Modifier
            .aspectRatio(1F)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .apply {
                if (selected) border(
                    width = 2.dp,
                    color = outlineColor,
                    shape = MaterialTheme.shapes.small,
                )
            },
    ) {
        val width = size.width
        val height = size.height
        // shape 1 - triangle bottom left
        val s1 = Path().apply {
            moveTo(0F, height)
            lineTo(width / 2, height)
            lineTo(0F, height / 2)
            close()
        }
        // shape 2 - poly above bottom left triangle
        val s2 = Path().apply {
            moveTo(0F, height / 2)
            lineTo(width / 2, height)
            lineTo(width, height)
            lineTo(0F, 0F)
            close()
        }
        // shape 3 - poly below top right triangle
        val s3 = Path().apply {
            moveTo(0F, 0F)
            lineTo(width, height)
            lineTo(width, height / 2)
            lineTo(width / 2, 0F)
            close()
        }
        // shape 4 - triangle top right
        val s4 = Path().apply {
            moveTo(width / 2, 0F)
            lineTo(width, height / 2)
            lineTo(width, 0F)
            close()
        }

        // draw colors
        drawIntoCanvas { canvas ->
            canvas.drawOutline(
                outline = Outline.Generic(s1),
                paint = Paint().apply { color = color1 },
            )
            canvas.drawOutline(
                outline = Outline.Generic(s2),
                paint = Paint().apply { color = color2 },
            )
            canvas.drawOutline(
                outline = Outline.Generic(s3),
                paint = Paint().apply { color = color3 },
            )
            canvas.drawOutline(
                outline = Outline.Generic(s4),
                paint = Paint().apply { color = color4 },
            )
        }
    }
}

@Composable
private fun AddCustomThemeButton(onAddTheme: () -> Unit, theme: InfernoTheme) {
    Column(
        modifier = Modifier
            .clickable(onClick = onAddTheme)
            .fillMaxWidth()
            .background(
                color = theme.secondaryBackgroundColor,
                shape = MaterialTheme.shapes.medium,
            )
            .border(
                width = 2.dp,
                color = theme.primaryOutlineColor,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1F)
                .fillMaxWidth()
                .background(theme.secondaryBackgroundColor, MaterialTheme.shapes.small)
                .border(
                    width = 2.dp,
                    color = theme.primaryOutlineColor,
                    shape = MaterialTheme.shapes.small,
                ),
            contentAlignment = Alignment.Center,
        ) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_new_24),
                contentDescription = "",
                modifier = Modifier.size(40.dp),
                tint = theme.primaryIconColor,
            )
        }
        // todo: string res
        InfernoText(text = "Add custom theme", fontColor = theme.primaryTextColor)
    }
}