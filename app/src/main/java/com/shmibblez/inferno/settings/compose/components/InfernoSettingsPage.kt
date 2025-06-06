package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.UiConst
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfernoSettingsPage(
    title: String,
    goBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_back_button_24),
                        contentDescription = stringResource(R.string.browser_menu_back),
                        modifier = Modifier
                            .padding(start = UiConst.TOP_BAR_INTERNAL_PADDING)
                            .size(18.dp)
                            .clickable(onClick = goBack),
                    )
                },
                title = {
                    InfernoText(
                        text = title,
                        infernoStyle = InfernoTextStyle.Title,
                        modifier = Modifier.padding(horizontal = UiConst.TOP_BAR_INTERNAL_PADDING),
                    )
                },
                colors = TopAppBarColors(
                    containerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
                    scrolledContainerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
                    navigationIconContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
                    titleContentColor = LocalContext.current.infernoTheme().value.primaryTextColor,
                    actionIconContentColor = LocalContext.current.infernoTheme().value.primaryActionColor,
                ),
            )
        },
        containerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
        content = { content.invoke(it) },
    )
}
