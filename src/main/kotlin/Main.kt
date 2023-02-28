import com.wakaztahir.appupdater.core.downloadUpdate
import com.wakaztahir.appupdater.core.getUpdateMetadata
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val updateMetadata = getUpdateMetadata("wakaztahir", "StringsTranslator", "1.0.0")
        val file = updateMetadata.response.downloadUpdate(
            onProgress = { bytesSent,contentLength ->
                try {
                    println("downloaded : $bytesSent out of $contentLength")
                }catch(_ : Exception){

                }
            }
        )
        println(file?.absolutePath)
    }
}