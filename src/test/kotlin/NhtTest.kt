package top.e404.managerDownloader

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.junit.Test
import top.e404.spider.Settings
import top.e404.spider.spider.Nht
import java.io.File
import java.util.concurrent.Executors

class NhtTest {
    @Test
    fun t1() {
        val pool = Executors.newFixedThreadPool(10)
        val d = Nht(392082)
        d.getList()
        val dir = File("nht")
        d.save(dir, pool)
    }

    @Test
    fun t2() {
        val url = "https://i5.nhentai.net/galleries/2271212/3.jpg"
        Jsoup.connect(url)
            .ignoreContentType(true)
            .proxy(Settings.proxy)
            .userAgent(Settings.userAgent)
            .header("bytes", "32768-32768")
            .cookie(" cf_clearance", "uVHzVXH.1iSFsQG.eR3hvoO3II9MHxzJ0gCmY7Hn8Ng-1658044308-0-150")
            .method(Connection.Method.GET)
            .execute()
    }
}