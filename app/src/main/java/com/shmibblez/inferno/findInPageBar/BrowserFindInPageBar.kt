package com.shmibblez.inferno.findInPageBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.R

@Composable
fun BrowserFindInPageBar() {
    var input by remember { mutableStateOf(TextFieldState("")) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(ComponentDimens.FIND_IN_PAGE_BAR_HEIGHT)
            .background(Color.Black),
    ) {
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .clickable { input = TextFieldState("") },
            tint = Color.White,
            painter = painterResource(id = R.drawable.mozac_ic_cross_20),
            contentDescription = "clear text"
        )
        BasicTextField(
            state = input, modifier = Modifier
                .wrapContentHeight()
                .weight(1F)
        )
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .clickable {
                    // TODO: find in page prev
                },
            tint = Color.White,
            painter = painterResource(id = R.drawable.mozac_ic_chevron_up_24),
            contentDescription = "prev. occurrence"
        )
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .clickable {
                    // TODO: find in page next
                },
            tint = Color.White,
            painter = painterResource(id = R.drawable.mozac_ic_chevron_down_24),
            contentDescription = "next occurrence"
        )
    }
}