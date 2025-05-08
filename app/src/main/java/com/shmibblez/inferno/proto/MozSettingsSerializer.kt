package com.shmibblez.inferno.proto

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object MozSettingsSerializer : Serializer<MozSettings> {
    override val defaultValue: MozSettings = MozSettings.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): MozSettings {
        try {
            return MozSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: MozSettings, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.mozSettingsDataStore: DataStore<MozSettings> by dataStore(
    fileName = "moz_settings.proto",
    serializer = MozSettingsSerializer
)