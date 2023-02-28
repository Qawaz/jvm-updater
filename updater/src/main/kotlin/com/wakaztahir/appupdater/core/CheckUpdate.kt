package com.wakaztahir.appupdater.core

import com.wakaztahir.appupdater.model.GithubAsset
import com.wakaztahir.appupdater.model.GithubResponse
import com.wakaztahir.appupdater.model.UpdateMetadata
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.io.File
import java.util.*

private fun String.parseAsVersion(): UpdateMetadata.Version {
    return if (contains('.')) {
        val arr = split('.')
        UpdateMetadata.Version(
            major = arr[0].toInt(),
            minor = arr[1].toIntOrNull() ?: 0,
            patch = arr[2].toIntOrNull() ?: 0
        )
    } else {
        UpdateMetadata.Version(
            major = this.toInt(),
            minor = 0,
            patch = 0
        )
    }
}

suspend fun getUpdateMetadata(owner: String, repo: String, currentVersion: String): UpdateMetadata {
    val version = currentVersion.parseAsVersion()
    val response = getGithubResponseFor(owner, repo)
    val releaseVersion = response.tagName.parseAsVersion()
    return UpdateMetadata(
        hasUpdate = releaseVersion > version,
        clientVersion = version,
        hostVersion = releaseVersion,
        response = response
    )
}

suspend inline fun UpdateMetadata.downloadAndLaunchUpdate(
    launchInExplorerAsWell: Boolean = false,
    noinline onProgress: (bytesSentTotal: Long, contentLength: Long) -> Unit
) {
    this.response.downloadAndLaunchUpdate(
        launchInExplorerAsWell = launchInExplorerAsWell,
        onProgress = onProgress
    )
}

suspend fun GithubResponse.downloadAndLaunchUpdate(
    launchInExplorerAsWell: Boolean = false,
    onProgress: suspend (bytesSentTotal: Long, contentLength: Long) -> Unit
) {
    val updatedFile = this.downloadUpdate(onProgress) ?: throw IllegalStateException("couldn't download file")
    if (launchInExplorerAsWell) {
        try {
            val desktop = Desktop.getDesktop()
            desktop?.browseFileDirectory(updatedFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    val processBuilder = ProcessBuilder(updatedFile.absolutePath)
    processBuilder.directory(updatedFile.parentFile)
    processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)
    withContext(Dispatchers.IO) {
        processBuilder.start().waitFor()
    }

}

private fun String.getExtension(): String {
    if (isEmpty()) return ""
    if (this[length - 1].let { it == '/' || it == '\\' || it == '.' }) {
        return ""
    }
    val dotInd = lastIndexOf('.')
    val sepInd = lastIndexOf('/').coerceAtLeast(lastIndexOf('\\'))
    return if (dotInd <= sepInd) "" else substring(dotInd + 1).lowercase(Locale.getDefault())
}

suspend fun GithubResponse.downloadUpdate(onProgress: suspend (bytesSentTotal: Long, contentLength: Long) -> Unit): File? {
    val currentOs = getOS() ?: throw IllegalStateException("couldn't get os")
    var assetWithExtension: Pair<GithubAsset, String>? = null

    // check files with extensions
    for (asset in assets) {
        val extension = asset.browserDownloadUrl.getExtension()
        if (currentOs.supportedExtensions.contains(extension)) {
            assetWithExtension = Pair(asset, extension)
            break
        }
    }

    // check files without extensions
    if (assetWithExtension == null && currentOs.supportNoExtension) {
        for (asset in assets) {
            val extension = asset.browserDownloadUrl.getExtension()
            if (extension == "") {
                assetWithExtension = Pair(asset, extension)
                break
            }
        }
    }

    return if (assetWithExtension != null) {
        downloadFile(
            url = assetWithExtension.first.browserDownloadUrl,
            extension = assetWithExtension.second,
            onProgress = onProgress
        )
    } else {
        null
    }
}

private suspend fun downloadFile(
    url: String,
    extension: String,
    onProgress: suspend (bytesSentTotal: Long, contentLength: Long) -> Unit
): File {
    return withContext(Dispatchers.IO) {
        val client = HttpClient {
            expectSuccess = false
            install(HttpTimeout)
        }
        val response = client.get(url) {
            timeout {
                this.requestTimeoutMillis = Long.MAX_VALUE
            }
            onDownload(onProgress)
        }
        val tempFile = File.createTempFile("update", ".$extension")
        val output = tempFile.outputStream()
        response.bodyAsChannel().copyTo(output)
        output.close()
        return@withContext tempFile
    }
}

suspend fun getGithubResponseFor(owner: String, repo: String, release: String = "latest"): GithubResponse {
    val repoUrl = "https://api.github.com/repos/$owner/$repo/releases/$release"
    val client = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    val response = client.get(repoUrl) {

    }
    return response.body<GithubResponse>()
}