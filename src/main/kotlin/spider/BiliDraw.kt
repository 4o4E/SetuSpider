package top.e404.spider.spider

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import okhttp3.ConnectionPool
import top.e404.spider.Settings
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.system.exitProcess

/**
 * 哔哩哔哩画集
 */
object BiliDraw {

    private val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
        install(UserAgent) {
            agent = Settings.userAgent
        }
        engine {
            config {
                connectionPool(ConnectionPool(10, 10, TimeUnit.MINUTES))
            }
        }
    }

    private val dir = File("F:/D/c").also { it.mkdirs() }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking(Dispatchers.IO) {
            download(375262988, 0).also {
                println("共获取到${it.size}张图片")
            }.map {
                async {
                    dir.resolve(it.substringAfterLast("/")).writeBytes(client.get(it).readBytes())
                }
            }.awaitAll()
            exitProcess(0)
        }
    }

    suspend fun download(uid: Long, page: Int): Set<String> {
        val data = data(uid, page) ?: return mutableSetOf()
        val total = data["total_count"]!!.jsonPrimitive.int
        if (total <= 50) return items(data)
        val set = HashSet<String>(total)
        set.addAll(items(data))
        val count = ceil(total / 50f).toInt() - 1
        repeat(count) {
            val d = data(uid, it + 2) ?: return@repeat
            set.addAll(items(d))
        }
        return set
    }

    suspend fun data(uid: Long, page: Int): JsonObject? {
        val jo = client.get("https://api.vc.bilibili.com/link_draw/v1/doc/others") {
            parameter("poster_uid", uid)
            parameter("page_num", page)
            parameter("page_size", "50")
        }.bodyAsText().let(Json.Default::parseToJsonElement).jsonObject
        if (jo["code"]?.jsonPrimitive?.int != 0) return null
        return jo["data"]!!.jsonObject
    }

    fun items(data: JsonObject) = data["items"]!!.jsonArray.flatMap { item ->
        item.jsonObject["pictures"]!!.jsonArray.map { picture ->
            picture.jsonObject["img_src"]!!.jsonPrimitive.content
        }
    }.toSet()
}