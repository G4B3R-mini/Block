package com.shmibblez.inferno.browser.nav

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.navigation.NavType
import androidx.savedstate.SavedState
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.Addon.Author
import mozilla.components.feature.addons.Addon.Companion.DEFAULT_LOCALE
import mozilla.components.feature.addons.Addon.DisabledReason
import mozilla.components.feature.addons.Addon.Incognito
import mozilla.components.feature.addons.Addon.InstalledState
import mozilla.components.feature.addons.Addon.Permission
import mozilla.components.feature.addons.Addon.Rating
import java.io.ByteArrayOutputStream

class AddonNavType : NavType<SerializableAddon>(isNullableAllowed = false) {
    override fun put(bundle: SavedState, key: String, value: SerializableAddon) {
        bundle.putSerializable(key, value)
    }

    @Suppress("DEPRECATION")
    override fun get(bundle: SavedState, key: String): SerializableAddon? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getSerializable(key, SerializableAddon::class.java)
        } else {
            bundle.getSerializable(key) as SerializableAddon
        }
    }

    override fun parseValue(value: String): SerializableAddon {
        return Json.decodeFromString(value)
    }

    override fun serializeAsValue(value: SerializableAddon): String {
        return Json.encodeToString(value)
    }
}

@Serializable
class SerializableAddon(
    val id: String,
    val author: SerializableAuthor? = null,
    val downloadUrl: String = "",
    val version: String = "",
    val permissions: List<String> = emptyList(),
    val optionalPermissions: List<SerializablePermission> = emptyList(),
    val optionalOrigins: List<SerializablePermission> = emptyList(),
    val translatableName: Map<String, String> = emptyMap(),
    val translatableDescription: Map<String, String> = emptyMap(),
    val translatableSummary: Map<String, String> = emptyMap(),
    val iconUrl: String = "",
    val homepageUrl: String = "",
    val rating: SerializableRating? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val icon: ByteArray? = null,
    val installedState: SerializableInstalledState? = null,
    val defaultLocale: String = DEFAULT_LOCALE,
    val ratingUrl: String = "",
    val detailUrl: String = "",
    val incognito: Incognito = Incognito.SPANNING,
) : java.io.Serializable {
    fun toAddon(): Addon {
        return Addon(
            id = id,
            author = author?.toAuthor(),
            downloadUrl = downloadUrl,
            version = version,
            permissions = permissions,
            optionalPermissions = optionalPermissions.toPermissionList(),
            optionalOrigins = optionalOrigins.toPermissionList(),
            translatableName = translatableName,
            translatableDescription = translatableDescription,
            translatableSummary = translatableSummary,
            iconUrl = iconUrl,
            homepageUrl = homepageUrl,
            rating = rating?.toRating(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            icon = icon?.toBitmap(),
            installedState = installedState?.toInstalledState(),
            defaultLocale = defaultLocale,
            ratingUrl = ratingUrl,
            detailUrl = detailUrl,
            incognito = incognito,
        )
    }
}

fun Addon.toSerializableAddon(): SerializableAddon {
    return SerializableAddon(
        id = id,
        author = author?.toSerializableAuthor(),
        downloadUrl = downloadUrl,
        version = version,
        permissions = permissions,
        optionalPermissions = optionalPermissions.toSerializablePermissionList(),
        optionalOrigins = optionalOrigins.toSerializablePermissionList(),
        translatableName = translatableName,
        translatableDescription = translatableDescription,
        translatableSummary = translatableSummary,
        iconUrl = iconUrl,
        homepageUrl = homepageUrl,
        rating = rating?.toSerializableRating(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        icon = icon?.toByteArray(),
        installedState = installedState?.toSerializableInstalledState(),
        defaultLocale = defaultLocale,
        ratingUrl = ratingUrl,
        detailUrl = detailUrl,
        incognito = incognito,
    )
}

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

@Serializable
data class SerializableAuthor(
    val name: String,
    val url: String,
) {
    fun toAuthor(): Author {
        return Author(
            name = name,
            url = url,
        )
    }
}

fun Author.toSerializableAuthor(): SerializableAuthor {
    return SerializableAuthor(
        name = this.name,
        url = this.url,
    )
}

@Serializable
data class SerializablePermission(
    val name: String,
    val granted: Boolean,
)

fun List<Permission>.toSerializablePermissionList(): List<SerializablePermission> {
    return this.map {
        SerializablePermission(
            name = it.name,
            granted = it.granted,
        )
    }
}

fun List<SerializablePermission>.toPermissionList(): List<Permission> {
    return this.map {
        Permission(
            name = it.name,
            granted = it.granted,
        )
    }
}

@Serializable
data class SerializableRating(
    val average: Float,
    val reviews: Int,
) {
    fun toRating(): Rating {
        return Rating(
            average = average,
            reviews = reviews,
        )
    }
}

fun Rating.toSerializableRating(): SerializableRating {
    return SerializableRating(
        average = average,
        reviews = reviews,
    )
}

data class SerializableBitmap(val bitmap: Bitmap) : java.io.Serializable {
    fun toBitmap(): Bitmap {
        return bitmap
    }
}

fun Bitmap.toSerializableBitmap(): SerializableBitmap {
    return SerializableBitmap(this)
}

@Serializable
data class SerializableInstalledState(
    val id: String,
    val version: String,
    val optionsPageUrl: String?,
    val openOptionsPageInTab: Boolean = false,
    val enabled: Boolean = false,
    val supported: Boolean = true,
    val disabledReason: DisabledReason? = null,
    val allowedInPrivateBrowsing: Boolean = false,
    val icon: ImageBitmap? = null,
) {
    fun toInstalledState(): InstalledState {
        return InstalledState(
            id = id,
            version = version,
            optionsPageUrl = optionsPageUrl,
            openOptionsPageInTab = openOptionsPageInTab,
            enabled = enabled,
            supported = supported,
            disabledReason = disabledReason,
            allowedInPrivateBrowsing = allowedInPrivateBrowsing,
            icon = icon?.asAndroidBitmap(),
        )
    }
}

fun InstalledState.toSerializableInstalledState(): SerializableInstalledState {
    return SerializableInstalledState(
        id = id,
        version = version,
        optionsPageUrl = optionsPageUrl,
        openOptionsPageInTab = openOptionsPageInTab,
        enabled = enabled,
        supported = supported,
        disabledReason = disabledReason,
        allowedInPrivateBrowsing = allowedInPrivateBrowsing,
        icon = icon?.asImageBitmap(),
    )
}