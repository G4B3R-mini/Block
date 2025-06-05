package com.shmibblez.inferno.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.library.history.History

@Composable
fun InfernoHistoryPage(goBack: () -> Unit) {
    val historyViewerState by rememberHistoryViewerState()
    val context = LocalContext.current

    Scaffold(topBar = {
        when (historyViewerState.mode) {
            is HistoryViewerState.Mode.Selection -> EditingTopBar(
                onStopEditing = { historyViewerState.stopEditing() },
                mode = historyViewerState.mode as HistoryViewerState.Mode.Selection,
            )

            HistoryViewerState.Mode.Syncing,
            HistoryViewerState.Mode.Normal,
                -> NormalTopBar(goBack = goBack)
        }
    }) { edgeInsets ->
        HistoryViewer(
            state = historyViewerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(edgeInsets)
                .padding(horizontal = 16.dp),
            onOpenHistoryItem = {
                when (it) {
                    is History.Group -> {}
                    is History.Metadata -> {
                        context.components.useCases.sessionUseCases.loadUrl(url = it.url)
                    }

                    is History.Regular -> {
                        context.components.useCases.sessionUseCases.loadUrl(url = it.url)
                    }
                }
                goBack.invoke()
            },
        )
    }
}

// todo: actions
//  - search
//  - trash / delete all
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopBar(goBack: () -> Unit) {
    TopAppBar(title = {
        InfernoText(
            text = stringResource(R.string.library_history),
            infernoStyle = InfernoTextStyle.Title,
        )
    }, navigationIcon = {
        InfernoIcon(
            painter = painterResource(R.drawable.ic_back_button_24),
            contentDescription = "",
            modifier = Modifier.clickable(onClick = goBack)
        )
    })
}

// todo: actions (3 dot menu with options)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditingTopBar(onStopEditing: () -> Unit, mode: HistoryViewerState.Mode.Selection) {
    TopAppBar(
        title = {
            InfernoText(
                text = stringResource(
                    R.string.history_multi_select_title,
                    mode.selectedItems.size.toString(),
                ),
                infernoStyle = InfernoTextStyle.Title,
            )
        },
        navigationIcon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_close_24),
                contentDescription = "",
                modifier = Modifier.clickable(onClick = onStopEditing)
            )
        },
    )
}