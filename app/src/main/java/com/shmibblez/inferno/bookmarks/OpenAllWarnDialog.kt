package com.shmibblez.inferno.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.settings.compose.components.PrefUiConst

private val LIST_BOTTOM_PADDING = 54.dp

@Composable
internal fun OpenAllWarnDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    numberOfTabs: Int,
) {
    InfernoDialog(
        onDismiss = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    InfernoText(
                        text = stringResource(R.string.open_all_warning_title, numberOfTabs),
                        infernoStyle = InfernoTextStyle.Title,
                    )
                }

                item {
                    InfernoText(
                        text = stringResource(R.string.open_all_warning_message, numberOfTabs),
                    )
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
                )
                InfernoButton(
                    text = stringResource(R.string.open_all_warning_confirm),
                    modifier = Modifier.weight(1F),
                    onClick = {
                        onConfirm.invoke()
                        onDismiss.invoke()
                    },
                )
            }
        }
    }
}