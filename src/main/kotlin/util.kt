package top.e404.spider

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun Any.log() = LoggerFactory.getLogger(this::class.java)!!

val clieht = HttpClient {
    install(UserAgent) {
        agent = Settings.userAgent
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 10000
    }
    engine {
        proxy = Settings.proxy
    }
}

suspend fun String.getAsBytes(block: HttpRequestBuilder.() -> Unit = {}) = clieht.get(this, block)

fun String.downloadAsImage(
    method: Connection.Method = Connection.Method.GET,
    handle: (Connection) -> Connection = { conn -> conn },
): ByteArray {
    for (i in 1..5) try {
        return Jsoup.connect(this)
            .proxy(Settings.proxy)
            .userAgent(Settings.userAgent)
            .ignoreContentType(true)
            .method(method)
            .timeout(30000)
            .let(handle)
            .execute()
            .bodyAsBytes()
    } catch (e: Exception) {
        println(this)
        e.printStackTrace()
    }
    throw Exception()
}

fun <T, R> handlerAsync(
    list: List<T>,
    handler: (T) -> R,
    threadCount: Int = 10,
    timeout: Long = 30L,
    timeUnit: TimeUnit = TimeUnit.MINUTES,
): CopyOnWriteArrayList<R> {
    val result = CopyOnWriteArrayList<R>()
    val count = list.size
    val ctp = Executors.newFixedThreadPool(if (count < threadCount) count else threadCount)
    for (i in 0 until count) ctp.execute { result.add(handler(list[i])) }
    ctp.shutdown()
    ctp.awaitTermination(timeout, timeUnit)
    return result
}