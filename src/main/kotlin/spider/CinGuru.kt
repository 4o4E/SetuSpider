package top.e404.spider.spider

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Connection
import org.jsoup.Jsoup
import top.e404.spider.Settings
import java.io.File

/**
 * https://cin.guru/
 */
object CinGuru {
    private const val origin = "https://cin.guru"
    private const val url = "https://cin.guru/v/"
    fun getImageUrls(id: Long, dir: File) {
        val jo = Jsoup.connect(url + id)
            .proxy(Settings.proxy)
            .get()
            .body()
            .selectFirst("script[id=__NEXT_DATA__]")!!
            .data()
            .let { JsonParser.parseString(it) }
            .asJsonObject["props"]
            .asJsonObject["pageProps"]
            .asJsonObject["data"]
            .asJsonObject
        val title = jo["title"].let { it.asJsonObject["japanese"]?.asString ?: it.asJsonObject["english"].asString }
        val images = jo["images"]
            .asJsonObject["pages"]
            .asJsonArray
            .map { it.asJsonObject["t"].asString }
        val d = dir.resolve(title)
        d.mkdirs()
        runBlocking(Dispatchers.IO) {
            images.forEach {
                launch {
                    val name = it.substringAfterLast("/")
                    try {
                        val bytes = Jsoup.connect(it)
                            .referrer("https://cin.guru/")
                            .proxy(Settings.proxy)
                            .header("origin", origin)
                            .userAgent(Settings.userAgent)
                            .method(Connection.Method.GET)
                            .ignoreContentType(true)
                            .execute()
                            .bodyAsBytes()
                        println(name)
                        d.resolve(name).writeBytes(bytes)
                    } catch (_: Throwable) {
                        try {
                            val bytes = Jsoup.connect(it)
                                .referrer("https://cin.guru/")
                                .header("origin", origin)
                                .proxy(Settings.proxy)
                                .userAgent(Settings.userAgent)
                                .method(Connection.Method.GET)
                                .ignoreContentType(true)
                                .execute()
                                .bodyAsBytes()
                            println(name)
                            d.resolve(name).writeBytes(bytes)
                        } catch (t: Throwable) {
                            println("$name - fail")
                        }
                    }
                }
            }
        }
    }
}