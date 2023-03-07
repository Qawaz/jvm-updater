package com.wakaztahir.appupdater.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private fun String.parseAsVersion(): UpdateMetadata.Version? {
    return try {
        val str = if (this[0] == 'v') removePrefix("v") else this
        if (str.contains('.')) {
            val arr = str.split('.')
            UpdateMetadata.Version(
                major = arr[0].toInt(),
                minor = arr.getOrNull(1)?.toIntOrNull() ?: 0,
                patch = arr.getOrNull(2)?.toIntOrNull() ?: 0
            )
        } else {
            UpdateMetadata.Version(
                major = str.toInt(),
                minor = 0,
                patch = 0
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

data class UpdateMetadata(
    val clientVersionStr: String,
    val hostVersionStr: String,
    val response: GithubResponse,
) {

    val clientVersion get() = clientVersionStr.parseAsVersion()

    val hostVersion get() = hostVersionStr.parseAsVersion()

    fun hasUpdate(): Boolean {
        if (hostVersion == null || clientVersion == null) return false
        return hostVersion!! > clientVersion!!
    }

    data class Version(val major: Int, val minor: Int, val patch: Int) {
        operator fun compareTo(other: Version): Int {
            return if (this.major == other.major && this.minor == other.minor && this.patch == other.patch) {
                0
            } else if (this.major >= other.major && this.minor >= other.minor && this.patch >= other.patch) {
                1
            } else if (this.major <= other.major && this.minor <= other.minor && this.patch <= other.patch) {
                -1
            } else {
                return 1
            }
        }
    }
}

@Serializable
data class GithubResponse(
    @SerialName("body") val body: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("tag_name") val tagName: String,
    @SerialName("assets") val assets: List<GithubAsset>
)

@Serializable
data class GithubAsset(
    @SerialName("state") val state: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String
)