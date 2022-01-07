package com.e404.spider

import org.jsoup.Connection
import org.jsoup.Jsoup
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private val agents = arrayOf(
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36 Edg/90.0.818.51",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:21.0) Gecko/20100101 Firefox/21.0",
    "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:21.0) Gecko/20130331 Firefox/21.0",
    "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0",
    "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19",
    "Mozilla/5.0 (Linux; Android 4.1.2; Nexus 7 Build/JZ054K) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Safari/535.19",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.93 Safari/537.36",
    "Mozilla/5.0 (compatible; WOW64; MSIE 10.0; Windows NT 6.2)",
    "Opera/9.80 (Windows NT 6.1; WOW64; U; en) Presto/2.10.229 Version/11.62",
    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27"
)

fun getAgent(): String {
    return agents[Random.nextInt(agents.size)]
}

fun String.downloadAsImage(
    method: Connection.Method = Connection.Method.GET,
    handle: (Connection) -> Connection = { conn -> conn },
): ByteArray {
    for (i in 1..5) try {
        return handle(Jsoup.connect(this)
            .userAgent(getAgent())
            .ignoreContentType(true)
            .method(method))
            .timeout(30000)
            .execute()
            .bodyAsBytes()
    } catch (e: Exception) {
        println(this)
        e.printStackTrace()
    }
    throw Exception()
}

fun <T> handlerAsync(
    list: List<Any>,
    handler: (Any) -> T,
    threadCount: Int = 10,
    timeout: Long = 30L,
    timeUnit: TimeUnit = TimeUnit.MINUTES,
): CopyOnWriteArrayList<T> {
    val result = CopyOnWriteArrayList<T>()
    val count = list.size
    val ctp = Executors.newFixedThreadPool(if (count < threadCount) count else threadCount)
    for (i in 0 until count) ctp.execute { result.add(handler(list[i])) }
    ctp.shutdown()
    ctp.awaitTermination(timeout, timeUnit)
    return result
}