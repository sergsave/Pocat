package com.sergsave.purryourcat.sharing

import com.sergsave.purryourcat.helpers.NetworkUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.io.*
import java.net.URL
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject

private val HOST = "gofile.io"

class GoFileIoNetworkService: NetworkService {
    override fun makeUploadObservable(file: File): Single<URL> {
        return GetServerName.sendRequest()
            .flatMap { serverName ->
                UploadFile.sendRequest(serverName, file)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun makeDownloadObservable(url: URL, destDir: File): Single<File> {
        return Single.fromCallable {
            val urlStr = url.toString()
            url.openStream().use { input ->
                val name = urlStr.substring(urlStr.lastIndexOf('/') + 1, urlStr.length)
                val file = File(destDir, name)
                FileOutputStream(file).use { output -> input.copyTo(output) }
                file
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

private object GetServerName {
    fun sendRequest() = Single.fromCallable {
        val response = NetworkUtils.sendGetRequest(URL("https://apiv2.$HOST/getServer"))
        parseServerName(response)
    }

    @Throws(IOException::class, JSONException::class)
    private fun parseServerName(response: String): String {
        return parseResponse(response).dataJson.getString("server")
    }
}

private object UploadFile {
    fun sendRequest(serverName: String, file: File) = Single.fromCallable {
        val url = URL("https://$serverName.$HOST/upload")
        val multipartSender = NetworkUtils.MultipartPostSender(url)

        multipartSender.addFilePart("filesUploaded", file)
        val response = multipartSender.finish()

        URL(composeDownloadLink(response, file.name, serverName))
    }

    @Throws(IOException::class, JSONException::class)
    private fun composeDownloadLink(response: String, fileName: String, serverName: String): String {
        val code = parseResponse(response).dataJson.getString("code")
        return "https://$serverName.$HOST/download/$code/$fileName"
    }
}

private data class Response(val status: String, val dataJson: JSONObject)
@Throws(IOException::class, JSONException::class)
private fun parseResponse(text: String): Response {
    val json = JSONObject(text)
    return Response(json.getString("status"), json.getJSONObject("data"))
}