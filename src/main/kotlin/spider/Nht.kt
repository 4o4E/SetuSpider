package top.e404.spider.spider

import org.jsoup.Connection
import org.jsoup.Jsoup
import top.e404.spider.Settings
import java.io.File
import java.util.concurrent.ExecutorService

/**
 * https://nhentai.to/
 */
class Nht(val id: Int) {
    companion object {
        private const val path = "https://nhentai.to"
        private const val token =
            "eyJpdiI6IjQ0eE5ZYzQweXc4K040U1lEdFJjeVE9PSIsInZhbHVlIjoiZFBkeG1KL0hReGowNDFtVWoxS1lCZkFFOGh6bmFJZWpSN3B5YmxnNU9vUTJha1grQWEyZXFRMUpsN1d2WWhPcSIsIm1hYyI6IjczN2I3ZmUzMDE0ODllMjU3NjJmNGFmNzAyZTY0MzA2ZTc5ZjkyMWU3NmZhNjc3ODU3ZmYxOGZhMmUyYTRkNGEifQ%3D%3D"
        private const val session =
            "eyJpdiI6IkJBUG45NnRhQ29EZnRDZGgvWWt6L1E9PSIsInZhbHVlIjoiSTFIa1pUTG1ibTVNTVZvRU9YeEMwbDlpOFRJdGc4a3BWMHVhTjAvMUh3SVU2MjkvTzl6YzNHNjhVdmV2NldQOCIsIm1hYyI6IjlmNzliMmVlYzdhNjE2ZmFmNTFhZjMzMGQ3Yzc1NzlmNTNhODI2ZDQ1MTk1NWRjNTZlM2FkYjc3OGIzMTNjOGEifQ%3D%3D"
    }

    //private val
    var urls = listOf<String>()
    var name = ""


    fun getList() {
        val url = "$path/g/$id"
        val body = Jsoup.connect(url)
            .userAgent(Settings.userAgent)
            .proxy(Settings.proxy)
            .referrer(url)
            .cookie("XSRF-TOKEN", token)
            .cookie("nhentai_session", session)
            .get()
            .body()
        name = body.select("#info > h2").text()
        urls = body.select("#thumbnail-container .thumb-container > a")
            .map { "$path${it.attr("href")}" }
        println("name: $name")
        println("pages: ${urls.size}")
    }

    fun save(dir: File, pool: ExecutorService) {
        val saveDir = dir.resolve(name).apply { mkdirs() }
        var done = 0
        urls.forEach { url ->
            pool.execute {
                var i = 0
                var t: Throwable? = null
                while (true) {
                    if (i == 10) {
                        done++
                        println("ERROR: $url")
                        t!!.printStackTrace()
                        return@execute
                    }
                    try {
                        val imageUrl = Jsoup.connect(url)
                            .ignoreContentType(true)
                            .timeout(10000)
                            .proxy(Settings.proxy)
                            .userAgent(Settings.userAgent)
                            .cookie("XSRF-TOKEN", token)
                            .cookie("nhentai_session", session)
                            .get()
                            .select("#image-container > a > img")
                            .attr("src")
                        val file = saveDir.resolve(imageUrl.split("/").last())
                        Jsoup.connect(imageUrl)
                            .ignoreContentType(true)
                            .userAgent(Settings.userAgent)
                            .proxy(Settings.proxy)
                            .method(Connection.Method.GET)
                            .execute()
                            .bodyAsBytes()
                            .let { file.writeBytes(it) }
                        done++
                        return@execute
                    } catch (tt: Throwable) {
                        t = tt
                        i++
                        Thread.sleep(1000)
                    }
                }
            }
        }
        while (done < urls.size) {
            println("$done / ${urls.size}")
            Thread.sleep(1000)
        }
        println("save done: ${saveDir.absolutePath}")
    }
}