package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.R

@Composable
fun InfernoDialog(
    onDismiss: () -> Unit,
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false,
    ),
    colors: CardColors = CardColors(
        containerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
        contentColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        disabledContainerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        disabledContentColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
    ),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = colors,
            border = border,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                horizontalAlignment = Alignment.Start,
                content = content,
            )
        }
    }
}

private val LIST_BOTTOM_PADDING = 58.dp

/**
 * this variant shows dismiss / confirm buttons, if [onConfirm] null, only shows cancel button
 */
@Composable
fun InfernoDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    dismissText: String = stringResource(android.R.string.cancel),
    onConfirm: (() -> Unit)?,
    confirmText: String = stringResource(R.string.browser_menu_save),
    confirmEnabled: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp, Alignment.Top),
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = true,
    ),
    colors: CardColors = CardColors(
        containerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
        contentColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        disabledContainerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        disabledContentColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
    ),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    backgroundContent: (@Composable () -> Unit)? = null,
    lazyContent: LazyListScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = colors,
            border = border,
        ) {
            Box(
                modifier = Modifier.padding(contentPadding).then(modifier),
                contentAlignment = Alignment.Center,
            ) {
                backgroundContent?.invoke()

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = verticalArrangement,
                ) {
                    lazyContent.invoke(this)
                    // bottom spacer for buttons
                    item { Spacer(Modifier.height(LIST_BOTTOM_PADDING)) }
                }

                // confirm / cancel buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    InfernoOutlinedButton(
                        text = dismissText,
                        modifier = Modifier.weight(1F),
                        onClick = onDismiss,
                    )
                    // if on confirm callback set, show on confirm
                    onConfirm?.let {
                        InfernoButton(
                            text = confirmText,
                            modifier = Modifier.weight(1F),
                            onClick = it,
                            enabled = confirmEnabled,
                        )
                    }
                }
            }
        }
    }
}