package com.shmibblez.inferno.proto

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

// todo: prefs
//   - group settings and space, first 15 are optimized but either way gud
//   - tabs
//     - existing settings
//       - list or grid in tabs tray
//       - when to close (never, after 1 day, etc -> make number value)
//       - move old tabs to inactive
//     - show x to close tabs on all or only on active
//     - tab min width (make percentage
//     - show or dont show tab bar
//     - show tab bar above or below toolbar
//   - toolbar
//     - use origin or mini origin
//     - icons / actions shown (reorderable list, on save modifies prefs)
//class SettingsSerializer() : Serializer<Settings> {
//    override val defaultValue: Settings = Settings.get
//    override suspend fun readFrom(input: InputStream): Settings {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun writeTo(t: Settings, output: OutputStream) {
//        TODO("Not yet implemented")
//    }
//}