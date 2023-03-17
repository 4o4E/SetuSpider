package top.e404.spider.spider

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import okhttp3.ConnectionPool
import top.e404.spider.Settings
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 哔哩哔哩动态
 */
object BiliDynamic {
    private const val url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/space"
    private val client = HttpClient(OkHttp) {
        install(UserAgent) {
            agent = Settings.userAgent
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
        engine {
            config {
                connectionPool(ConnectionPool(10, 10, TimeUnit.MINUTES))
            }
        }
    }

    private fun JsonElement.getAsJsonObjectOrNull(name: String) = jsonObject[name]?.let { if (it is JsonNull) null else it }

    suspend fun get(id: String, offset: String = "", set: MutableSet<String>): String? {
        val jo = client.get(url.replace("{id}", id)) {
            parameter("offset", offset)
            parameter("host_mid", id)
            userAgent(Settings.userAgent)
            header("referer", "https://www.bilibili.com/")
        }.bodyAsText().let {
            Json.encodeToJsonElement(it)
        }.jsonObject
        val code = jo["code"]!!.jsonPrimitive.int
        if (code != 0) throw RuntimeException("non zero return code: $code")
        val data = jo["data"]!!.jsonObject
        val hasMore = data["has_more"]!!.jsonPrimitive.boolean
        data["items"]!!.jsonArray.forEach { item ->
            item as JsonObject
            try {
                val draw = item.getAsJsonObjectOrNull("modules")
                    ?.getAsJsonObjectOrNull("module_dynamic")
                    ?.getAsJsonObjectOrNull("major")
                    ?.getAsJsonObjectOrNull("draw")
                    ?: return@forEach
                draw.jsonObject["items"]!!.jsonArray.forEach {
                    set.add(it.jsonObject["src"]!!.jsonPrimitive.content)
                }
            } catch (e: Exception) {
                println(item)
                throw e
            }
        }
        if (!hasMore) return null
        return data.jsonObject["offset"]!!.jsonPrimitive.content
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val id = "375262988"
        val saveDir = File("F:/D/dir").also { it.mkdirs() }
        val set = mutableSetOf<String>()

        runBlocking(Dispatchers.IO) {
            var next = ""
            while (true) {
                next = get(id, next, set) ?: break
            }

            set.map {
                async {
                    println(it)
                    saveDir.resolve(it.substringAfterLast("/")).writeBytes(client.get(it).readBytes())
                }
            }.awaitAll()
        }
    }
}