package com.shmibblez.inferno.findInPageBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.findTabOrCustomTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.lib.state.ext.flowScoped

private val ICON_SIZE = 20.dp

@Composable
fun BrowserFindInPageBar(
    onDismiss: () -> Unit,
    engineSession: EngineSession?,
    engineView: EngineView?,
    session: TabSessionState?
) {
    val context = LocalContext.current
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var resultCount by remember { mutableStateOf("") }
    var resultIsError by remember { mutableStateOf(false) }
    var resultCountAccessibility by remember { mutableStateOf("") }

    DisposableEffect(engineSession) {
        val scope = context.components.core.store.flowScoped { flow ->
            flow.mapNotNull { state -> session?.let { state.findTabOrCustomTab(it.id) } }
                .distinctUntilChangedBy { it.content.findResults }.collect {
                    val results = it.content.findResults
                    if (results.isNotEmpty()) {
                        val result = results.last()
                        // display result
                        with(result) {
                            val ordinal =
                                if (numberOfMatches > 0) activeMatchOrdinal + 1 else activeMatchOrdinal
                            resultCount = "$ordinal/$numberOfMatches"
                            resultIsError = numberOfMatches <= 0
                            resultCountAccessibility = context.getString(
                                R.string.mozac_feature_findindpage_accessibility_result,
                                ordinal,
                                numberOfMatches
                            )
                        }
                    }
                }
        }
        onDispose {
            scope.cancel()
            engineSession?.clearFindMatches()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ComponentDimens.FIND_IN_PAGE_BAR_HEIGHT)
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable(onClick = onDismiss),
            tint = Color.White,
            painter = painterResource(id = R.drawable.mozac_ic_cross_20),
            contentDescription = "exit"
        )
        TextField(
            value = input,
            onValueChange = {
                val newQuery = it.text
                input = it
                if (newQuery.isNotBlank()) {
                    // on query change, find all
                    engineSession?.findAll(newQuery)
//                emitCommitFact(query)
                } else {
                    // reset
                    engineSession?.clearFindMatches()
                    input = TextFieldValue("")
                }
            },
            modifier = Modifier.weight(1F),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 16.sp,
//                color = Color.White,
                letterSpacing = 0.15.sp,
                textAlign = TextAlign.Start,
            ),
            isError = resultIsError,
//            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                errorTextColor = Color.Red,
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
                errorContainerColor = Color.Black,
            ),
        )
        InfernoText(
            text = resultCount,
            fontColor = if (resultIsError) Color.Red else Color.LightGray,
        )
        // prev
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable {
                    // previous results
                    engineSession?.findNext(forward = false)
                    engineView
                        ?.asView()
                        ?.clearFocus()
                    // todo: hide keyboard
//                    view.asView().hideKeyboard()
//                    emitPreviousFact()
                },
            tint = Color.White,
            painter = painterResource(id = R.drawable.mozac_ic_chevron_up_24),
            contentDescription = "prev. occurrence"
        )
        // next
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable {
                    // next results
                    engineSession?.findNext(forward = true)
                    engineView
                        ?.asView()
                        ?.clearFocus()
                    // todo: hide keyboard
//                    view.asView().hideKeyboard()
//                    emitNextFact()
                },
            tint = Color.White,
            painter = painterResource(id = R.drawable.mozac_ic_chevron_down_24),
            contentDescription = "next occurrence"
        )
    }
}