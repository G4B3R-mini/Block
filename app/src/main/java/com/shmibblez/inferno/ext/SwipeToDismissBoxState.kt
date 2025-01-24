package com.shmibblez.inferno.ext

import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue

fun SwipeToDismissBoxState.isDismissed(swipeToDismissBoxValue: SwipeToDismissBoxValue):Boolean {
    return this.dismissDirection == swipeToDismissBoxValue && this.currentValue == this.targetValue
}