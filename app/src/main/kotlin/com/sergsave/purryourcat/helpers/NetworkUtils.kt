package com.sergsave.purryourcat.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION") return nwInfo.isConnected
        }
    }

    @Throws(IOException::class)
    fun sendGetRequest(url: URL): String {
        var connection: HttpURLConnection? = null
        return try {
            connection = (url.openConnection() as? HttpURLConnection)
            if(connection == null) {
                throw IOException("Non http url")
            }

            connection.run {
                requestMethod = "GET"
                doInput = true
                connect()
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("HTTP error code: $responseCode")
                }

                inputStream.bufferedReader().use(BufferedReader::readText)
            }
        } finally {
            connection?.disconnect()
        }
    }

    // https://www.codejava.net/java-se/networking/upload-files-by-sending-multipart-request-programmatically
    class MultipartPostSender
    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     * @param url
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    constructor(url: URL) {

        companion object {
            private const val LINE_FEED = "\r\n"
            private const val charset = "UTF-8"
        }

        // creates a unique boundary based on time stamp
        private val boundary: String = "===" + System.currentTimeMillis() + "==="
        private val httpConnection: HttpURLConnection
        private val outputStream: OutputStream
        private val writer: PrintWriter

        init {
            val connection = url.openConnection() as? HttpURLConnection
            if (connection == null) {
                throw IOException("Not http url")
            }
            httpConnection = connection.apply {
                setRequestProperty("User-Agent", "CodeJava Agent")
                setRequestProperty("Test", "Bonjour")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                doInput = true
                doOutput = true    // indicates POST method
                useCaches = false
            }
            outputStream = httpConnection.outputStream
            writer = PrintWriter(OutputStreamWriter(outputStream, charset), true)
        }

        fun addFormField(name: String, value: String) {
            writer.append("--").append(boundary).append(LINE_FEED)
            writer.append("Content-Disposition: form-data; name=\"$name\"")
                .append(LINE_FEED)
            writer.append("Content-Type: text/plain; charset=$charset").append(
                LINE_FEED)
            writer.append(LINE_FEED)
            writer.append(value).append(LINE_FEED)
            writer.flush()
        }

        @Throws(IOException::class)
        fun addFilePart(fieldName: String, uploadFile: File) {
            val fileName = uploadFile.name
            val fileType = URLConnection.guessContentTypeFromName(fileName)

            writer.append("--").append(boundary).append(LINE_FEED)
            writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\""
            )
                .append(LINE_FEED)
            writer.append("Content-Type: $fileType").append(LINE_FEED)
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED)
            writer.append(LINE_FEED)
            writer.flush()

            val inputStream = FileInputStream(uploadFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            inputStream.close()
            writer.append(LINE_FEED)
            writer.flush()
        }

        fun addHeaderField(name: String, value: String) {
            writer.append("$name: $value").append(LINE_FEED)
            writer.flush()
        }

        @Throws(IOException::class)
        fun finish(): String {
            writer.append(LINE_FEED).flush()
            writer.append("--").append(boundary).append("--")
                .append(LINE_FEED)
            writer.close()

            return try {
                if (httpConnection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("HTTP error code: ${httpConnection.responseCode}")
                }
                httpConnection.inputStream.bufferedReader().use(BufferedReader::readText)
            } finally {
                httpConnection.disconnect()
            }
        }
    }
}