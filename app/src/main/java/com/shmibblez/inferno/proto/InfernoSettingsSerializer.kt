package com.shmibblez.inferno.proto

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
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
object InfernoSettingsSerializer : Serializer<InfernoSettings> {
    override val defaultValue: InfernoSettings = InfernoSettings.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): InfernoSettings {
        try {
            return InfernoSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: InfernoSettings, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.infernoSettingsDataStore: DataStore<InfernoSettings> by dataStore(
    fileName = "inferno_settings.proto",
    serializer = InfernoSettingsSerializer
)