/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.compose.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.platform.AccessibilityManager
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.shmibblez.inferno.compose.core.Action
import com.shmibblez.inferno.compose.snackbar.SnackbarState.Duration.Preset.Indefinite
import com.shmibblez.inferno.compose.snackbar.SnackbarState.Duration.Preset.Short
import com.shmibblez.inferno.compose.snackbar.SnackbarState.Type
import kotlin.math.abs

private val defaultDuration = SnackbarState.Duration.Preset.Short
private val defaultType = Type.Default
private val defaultAction: Action? = null
private val defaultOnDismiss: () -> Unit = {}

// TODO: magic numbers adjustment
fun SnackbarDuration.toMillis(
    hasAction: Boolean,
    accessibilityManager: AccessibilityManager?
): Long {
    val original =
        when (this) {
            SnackbarDuration.Indefinite -> Long.MAX_VALUE
            SnackbarDuration.Long -> 10000L
            SnackbarDuration.Short -> 4000L
        }
    if (accessibilityManager == null) {
        return original
    }
    return accessibilityManager.calculateRecommendedTimeoutMillis(
        original,
        containsIcons = true,
        containsText = true,
        containsControls = hasAction
    )
}

/**
 * The data to display within a Snackbar.
 *
 * @property message The text to display within a Snackbar.
 * @property duration The duration of the Snackbar.
 * @property type The [Type] used to apply styling.
 * @property action Optional action within the Snackbar.
 * @property onDismiss Invoked when the Snackbar is dismissed.
 */
data class SnackbarState(
    val message: String,
    val duration: Duration = defaultDuration,
    val type: Type = defaultType,
    val action: Action? = defaultAction,
    val onDismiss: () -> Unit = defaultOnDismiss,
) {

    /**
     * A sealed type to represent a Snackbar's display duration.
     */
    sealed interface Duration {

        fun toSnackbarDuration(): SnackbarDuration {
            if (this is Preset) {
                return when (this) {
                    Indefinite -> SnackbarDuration.Indefinite
                    Preset.Long -> SnackbarDuration.Long
                    Short -> SnackbarDuration.Long
                }
            } else if (this is Custom) {
                val durations = arrayOf(4000, 10000, Int.MAX_VALUE)
                var duration = durations[0]
                for (n in durations)
                    if (abs(duration - n) < abs(this.durationMs - n))
                        duration = n
                return when (duration) {
                    4000 -> SnackbarDuration.Short
                    10000 -> SnackbarDuration.Long
                    Int.MAX_VALUE -> SnackbarDuration.Indefinite
                    else -> {
                        throw Error("this should not happen")
                    }
                }
            }
            throw Error("this should not happen")
        }

        /**
         * A predefined display duration.
         */
        enum class Preset(val durationMs: Int) : Duration {
            Indefinite(durationMs = Int.MAX_VALUE),
            Long(durationMs = 10000),
            Short(durationMs = 4000);
        }

        /**
         * A custom display duration.
         *
         * @property durationMs The duration in milliseconds.
         */
        data class Custom(val durationMs: Int) : Duration {
        }
    }

    /**
     * Get the display duration of the Snackbar in milliseconds.
     */
    val durationMs: Int
        get() = when (duration) {
            is Duration.Preset -> duration.durationMs
            is Duration.Custom -> duration.durationMs
        }

    /**
     * Convert [SnackbarState.Duration] to [SnackbarDuration].
     */
    fun toSnackbarDuration(): SnackbarDuration {
        return when (duration) {
            Duration.Preset.Indefinite -> SnackbarDuration.Indefinite
            Duration.Preset.Long -> SnackbarDuration.Long
            Duration.Preset.Short -> SnackbarDuration.Short
            is Duration.Custom -> SnackbarDuration.Short
        }
    }

    /**
     * The type of Snackbar to display.
     */
    enum class Type {
        Default,
        Warning,
    }
}

/**
 * Helper function to convert a Material Integer constant to a [SnackbarState.Duration].
 */
fun Int.toSnackbarDuration(): SnackbarState.Duration = when (this) {
    LENGTH_SHORT -> SnackbarState.Duration.Preset.Short
    LENGTH_LONG -> SnackbarState.Duration.Preset.Long
    LENGTH_INDEFINITE -> SnackbarState.Duration.Preset.Indefinite
    else -> SnackbarState.Duration.Custom(durationMs = this)
}