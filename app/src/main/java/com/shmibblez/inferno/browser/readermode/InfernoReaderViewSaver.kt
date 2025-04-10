package com.shmibblez.inferno.browser.readermode

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope

fun <Original, Saveable> infernoReaderViewSaver(
    save: SaverScope.(value: Original) -> List<Saveable>,
    restore: (list: List<Saveable>) -> Original?
): Saver<Original, Any> = @Suppress("UNCHECKED_CAST") (Saver(
        save = {
            val list = save(it)
            for (index in list.indices) {
                val item = list[index]
                if (item != null) {
                    require(canBeSaved(item)) { "item can't be saved" }
                }
            }
            if (list.isNotEmpty()) ArrayList(list) else null
        },
        restore = restore as (Any) -> Original?
    ))