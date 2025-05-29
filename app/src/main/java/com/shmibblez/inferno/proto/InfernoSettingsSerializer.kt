package com.shmibblez.inferno.proto

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object InfernoSettingsSerializer : Serializer<InfernoSettings> {
    override val defaultValue: InfernoSettings = InfernoSettings.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): InfernoSettings {
        InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM
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