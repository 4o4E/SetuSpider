package top.e404.spider

import io.ktor.client.engine.*

object Settings {
    const val proxyHost = "localhost"
    const val proxyPort = 7890
    val proxy = ProxyBuilder.socks(proxyHost, proxyPort)
    const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
}