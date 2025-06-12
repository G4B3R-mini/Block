package com.shmibblez.inferno.compose

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.ext.infernoTheme

private val STAR_SIZE = 12.dp

@Composable
fun StarRating(@FloatRange(0.0, 5.0) rating: Float, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0..4) {
            val percent = (rating - i).coerceIn(0F, 1F)
            if (percent < 1) {
                PartialStar(percent)
            } else {
                FullStar()
            }
        }
    }
}

@Composable
private fun FullStar() {
    InfernoIcon(
        painter = painterResource(R.drawable.ic_star_24),
        contentDescription = "",
        modifier = Modifier.size(STAR_SIZE),
        tint = LocalContext.current.infernoTheme().value.primaryActionColor,
    )
}

@Composable
private fun PartialStar(@FloatRange(0.0, 1.0) percent: Float) {
    Box(
        modifier = Modifier.size(STAR_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        InfernoIcon(
            painter = painterResource(R.drawable.ic_star_24),
            contentDescription = "",
            modifier = Modifier.size(STAR_SIZE),
            tint = LocalContext.current.infernoTheme().value.secondaryIconColor,
        )
        if (percent > 0) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_star_24),
                contentDescription = "",
                modifier = Modifier
                    .size(STAR_SIZE)
                    .graphicsLayer {
                        clip = true
                        shape = object : Shape {
                            override fun createOutline(
                                size: Size,
                                layoutDirection: LayoutDirection,
                                density: Density,
                            ): Outline {
                                return Outline.Rectangle(
                                    Rect(
                                        topLeft = Offset(0F, 0F),
                                        bottomRight = Offset(size.width * percent, size.height),
                                    )
                                )
                            }

                        }
                    },
                tint = LocalContext.current.infernoTheme().value.primaryActionColor,
            )
        }
    }
}