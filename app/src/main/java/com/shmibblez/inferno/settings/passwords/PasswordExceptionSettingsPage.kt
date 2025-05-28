package com.shmibblez.inferno.settings.passwords

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.toolbar.InfernoLoadingSquare

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PasswordExceptionSettingsPage(goBack: () -> Unit) {
    InfernoSettingsPage(
        title = "Password Exceptions", // todo: string res
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            item {
                InfernoText("we haven't made this page yet, check out this sick animation though")
            }

            item {
                InfernoText("\n\n>:(\n\n")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    InfernoLoadingSquare(size = 72.dp)
                }
            }
        }
    }
}