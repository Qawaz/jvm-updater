package com.wakaztahir.appupdater.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class UpdateMetadata(
    val hasUpdate: Boolean,
    val clientVersion: Version,
    val hostVersion: Version,
    val response: GithubResponse,
) {
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