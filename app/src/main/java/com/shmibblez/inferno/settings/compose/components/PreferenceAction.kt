package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText

@Composable
fun PreferenceAction(title: String, action: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = action)
            .padding(
                horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // title
        InfernoText(text = title)
        // action chevron
        InfernoIcon(
            painter = painterResource(R.drawable.ic_chevron_right_24),
            contentDescription = stringResource(R.string.phone_feature_go_to_settings),
            modifier = Modifier.size(18.dp),
        )
    }
}