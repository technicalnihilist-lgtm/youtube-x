package com.youtubex.app.data

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

/**
 * NewPipeExtractor ko network dene ke liye hamara downloader.
 * Yahan HttpURLConnection use kiya gaya hai taaki koi extra library na chahiye.
 */
class DownloaderImpl private constructor() : Downloader() {

    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val url = request.url()
        val method = request.httpMethod()
        val dataToSend = request.dataToSend()

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 30_000
            requestMethod = method
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept-Language", "en-GB,en;q=0.9")
            setRequestProperty("Accept", "*/*")
        }

        request.headers()?.forEach { (name, values) ->
            if (name == null) return@forEach
            values.forEach { value -> connection.addRequestProperty(name, value) }
        }

        if (dataToSend != null && dataToSend.isNotEmpty()) {
            connection.doOutput = true
            connection.outputStream.use { it.write(dataToSend) }
        }

        val responseCode = connection.responseCode
        val responseMessage = connection.responseMessage ?: ""
        val latestUrl = connection.url?.toString() ?: url

        if (responseCode == 429) {
            connection.disconnect()
            throw ReCaptchaException("reCaptcha challenge requested", url)
        }

        val bodyStream = if (responseCode >= 400) connection.errorStream else connection.inputStream
        val body = try {
            bodyStream?.use { raw ->
                val input = if ("gzip".equals(connection.contentEncoding, ignoreCase = true)) {
                    GZIPInputStream(raw)
                } else {
                    raw
                }
                input.readBytes().toString(Charsets.UTF_8)
            } ?: ""
        } catch (e: IOException) {
            ""
        }

        val headers: MutableMap<String, List<String>> = HashMap()
        connection.headerFields.forEach { (name, values) ->
            if (name != null && values != null) headers[name] = values
        }
        connection.disconnect()

        return Response(responseCode, responseMessage, headers, body, latestUrl)
    }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        @Volatile
        private var instance: DownloaderImpl? = null

        @JvmStatic
        fun getInstance(): DownloaderImpl =
            instance ?: synchronized(this) {
                instance ?: DownloaderImpl().also { instance = it }
            }
    }
}
