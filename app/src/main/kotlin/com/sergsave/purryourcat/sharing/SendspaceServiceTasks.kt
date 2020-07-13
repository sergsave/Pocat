package com.sergsave.purryourcat.sharing

import android.os.AsyncTask
import android.util.Xml
import com.sergsave.purryourcat.helpers.NetworkUtils
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.*
import java.net.URL
import java.net.URLConnection

// TODO: Separate library

class SendspaceUploadTask : AsyncTask<File, Void, URL?>() {

    override fun doInBackground(vararg params: File?): URL? {
        val file = params.get(0)
        if (isCancelled || file == null)
            return null

        try {
            val getResult = sendGetUploadInfoRequest()
            if(getResult == null || getResult.info == null)
                return null

            val postResult = sendPostUploadRequest(file, getResult)
            return postResult?.downloadUrl
        } catch (e: Exception) {
            return null
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun sendGetUploadInfoRequest(): UploadGetInfoResponseParser.Result? {
        val url =
            URL("http://api.sendspace.com/rest/?method=anonymous.uploadGetInfo&api_key=$API_KEY&api_version=1.2")

        var result: UploadGetInfoResponseParser.Result? = null
        val parseStream = { stream: InputStream? ->
            result = stream?.let { UploadGetInfoResponseParser().parse(it) }
        }
        NetworkUtils.sendGetRequest(url, parseStream)
        return result
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun sendPostUploadRequest(file: File, getResult: UploadGetInfoResponseParser.Result):
            UploadPostResponseParser.Result? {
        val multipartSender = NetworkUtils.MultipartPostSender(getResult.info!!.uploadUrl!!)

        multipartSender.addFormField("MAX_FILE_SIZE", getResult.info!!.maxFileSize!!.toString())
        multipartSender.addFormField("UPLOAD_IDENTIFIER", getResult.info!!.uploadIdentifier!!)
        multipartSender.addFormField("extra_info", getResult.info!!.extraInfo!!)
        multipartSender.addFilePart("userfile", file)

        var result: UploadPostResponseParser.Result? = null
        val parseStream = { stream: InputStream? ->
            result = stream?.let { UploadPostResponseParser().parse(it) }
        }
        multipartSender.finish(parseStream)
        return result
    }

//    protected fun onPostExecute(vararg result: URL?) {
//    }
}

class SendspaceDownloadTask(val destDir: File) : AsyncTask<URL, Void, File?>() {
    override fun doInBackground(vararg params: URL?): File? {
        val url = params.get(0)
        if (isCancelled || url == null)
            return null

        try {
            return downloadFile(url)
        } catch (e: Exception) {
            return null
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun sendGetDownloadInfoRequest(): UploadGetInfoResponseParser.Result? {
        val url =
            URL("http://api.sendspace.com/rest/?method=anonymous.uploadGetInfo&api_key=$API_KEY&api_version=1.2")

        var result: UploadGetInfoResponseParser.Result? = null
        val parseStream = { stream: InputStream? ->
            result = stream?.let { UploadGetInfoResponseParser().parse(it) }
        }
        NetworkUtils.sendGetRequest(url, parseStream)
        return result
    }

    @Throws(IOException::class, FileNotFoundException::class)
    private fun downloadFile(url: URL): File {
        val conn: URLConnection = url.openConnection()
        val contentLength = conn.getContentLength()
        val stream = DataInputStream(url.openStream())
        val buffer = ByteArray(contentLength)

        stream.readFully(buffer)
        stream.close()

        destDir.mkdirs()
        val outputFile = File(destDir, "name")
        val fos = DataOutputStream(FileOutputStream(outputFile))
        fos.write(buffer)
        fos.flush()
        fos.close()
        return outputFile
    }
}

private interface IXmlReader<T> {
    fun read(parser: XmlPullParser): T
}

@Throws(IOException::class, XmlPullParserException::class)
private fun <T> parseXmlResponse(inputStream: InputStream, reader: IXmlReader<T>): T {
    inputStream.use { stream ->
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(stream, null)
        parser.nextTag()
        return reader.read(parser)
    }
}

private val API_KEY = "8509M91WFY"
private val ERROR_TAG = "error"

@Throws(XmlPullParserException::class, IOException::class)
private fun skip(parser: XmlPullParser) {
    if (parser.eventType != XmlPullParser.START_TAG) {
        throw IllegalStateException()
    }
    var depth = 1
    while (depth != 0) {
        when (parser.next()) {
            XmlPullParser.END_TAG -> depth--
            XmlPullParser.START_TAG -> depth++
        }
    }
}

private data class Error (
    val code: Int? = null,
    val text: String? = null
)

@Throws(IOException::class, XmlPullParserException::class)
private fun readError(parser: XmlPullParser): Error {
    parser.require(XmlPullParser.START_TAG, null, ERROR_TAG)

    val ret = Error(
        code = parser.getAttributeValue(null, "code")?.toIntOrNull(),
        text = parser.getAttributeValue(null, "text")
    )

    parser.nextTag()
    parser.require(XmlPullParser.END_TAG, null, ERROR_TAG)
    return ret
}

private class UploadGetInfoResponseParser: IXmlReader<UploadGetInfoResponseParser.Result> {

    data class Result (
        val info: Info? = null,
        val error: Error? = null
    )

    data class Info(
        val uploadUrl: URL? = null,
        val progressUrl: URL? = null,
        val maxFileSize: Long? = null,
        val uploadIdentifier: String? = null,
        val extraInfo: String? = null
    )

    @Throws(IOException::class, XmlPullParserException::class)
    override fun read(parser: XmlPullParser): Result {
        parser.require(XmlPullParser.START_TAG, null, "result")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                UPLOAD_TAG -> return Result(readUploadInfo(parser), null)
                ERROR_TAG -> return Result(null, readError(parser))
                else -> skip(parser)
            }
        }

        return Result()
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readUploadInfo(parser: XmlPullParser): Info {
        parser.require(XmlPullParser.START_TAG, null, UPLOAD_TAG)

        val ret = Info(
            uploadUrl = parser.getAttributeValue(null, "url")?.let{ URL(it) },
            progressUrl = parser.getAttributeValue(null, "progress_url")?.let{ URL(it) },
            maxFileSize = parser.getAttributeValue(null, "max_file_size")?.toLongOrNull(),
            uploadIdentifier = parser.getAttributeValue(null, "upload_identifier"),
            extraInfo = parser.getAttributeValue(null, "extra_info")
        )

        parser.nextTag()
        parser.require(XmlPullParser.END_TAG, null, UPLOAD_TAG)
        return ret
    }

    companion object {
        private val UPLOAD_TAG = "upload"
    }

}

private class UploadPostResponseParser: IXmlReader<UploadPostResponseParser.Result> {

    data class Result(
        val isOk: Boolean? = null,
        val downloadUrl: URL? = null,
        val deleteUrl: URL? = null,
        val fileId: String? = null,
        val fileName: String? = null
    )

    @Throws(IOException::class, XmlPullParserException::class)
    override fun read(parser: XmlPullParser): Result {
        parser.require(XmlPullParser.START_TAG, null, "upload_done")

        var isOk: Boolean? = null
        var downloadUrl: URL? = null
        var deleteUrl: URL? = null
        var fileId: String? = null
        var fileName: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val value = readValue(parser, parser.name)
            when (parser.name) {
                "status" -> isOk = (value == "ok")
                "download_url" -> downloadUrl = URL(value)
                "delete_url" -> deleteUrl = URL(value)
                "file_id" -> fileId = value
                "file_name" -> fileName = value
            }
        }
        return Result(isOk, downloadUrl, deleteUrl, fileId, fileName)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readValue(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, null, tag)

        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }

        parser.require(XmlPullParser.END_TAG, null, tag)
        return result
    }
}


private class DownloadGetInfoResponseParser: IXmlReader<DownloadGetInfoResponseParser.Result> {

    data class Result (
        val info: Info? = null,
        val error: Error? = null
    )

    data class Info(
        val fileSize: Long? = null,
        val fileId: String? = null,
        val name: String? = null,
        val url: URL? = null
    )

    @Throws(IOException::class, XmlPullParserException::class)
    override fun read(parser: XmlPullParser): Result {
        parser.require(XmlPullParser.START_TAG, null, "result")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                UPLOAD_TAG -> return Result(readUploadInfo(parser), null)
                ERROR_TAG -> return Result(null, readError(parser))
                else -> skip(parser)
            }
        }

        return Result()
    }

    companion object {
        val DOWNLOAD_TAG = ""
    }
}
