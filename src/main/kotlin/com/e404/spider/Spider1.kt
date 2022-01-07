package com.e404.spider

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess


@Suppress("UNCHECKED_CAST")
object SpiderNiJi {
    @JvmStatic
    private val log = LoggerFactory.getLogger("")
    private const val url = "https://www.niji-wired.info/page/{p}/"

    /**
     * 通过目录url获得所有帖子的信息列表
     *
     * @return list<pair<title, url>>
     */
    @JvmStatic
    fun getPosts(): List<Pair<String, String>> {
        val postInfoList = handlerAsync((1..94).map { url.replace("{p}", it.toString()) },
            fun(a: Any): List<Pair<String, String>> {
                val url = a as String
                val l = try {
                    Jsoup.connect(url).get()
                        .select(".listCT li > a")
                        .map { it.attr("title") to it.attr("href") }
                } catch (ignore: Exception) {
                    try {
                        Jsoup.connect(url).get()
                            .select(".listCT li > a")
                            .map { it.attr("title") to it.attr("href") }
                    } catch (e: Exception) {
                        log.warn("解析目录`${url}`时出现异常", e)
                        emptyList()
                    }
                }
                log.debug("目录解析完成, url: $url")
                return l
            }, 30)
            .flatMap { it.asIterable() }
        log.info("目录解析完成, 共${postInfoList.size}个帖子")
        return postInfoList
    }

    /**
     * 通过帖子url获得其中图片url列表
     *
     * @param postInfoList 帖子信息列表
     * @return map<title, list<url>>
     */
    @JvmStatic
    private fun spiderPosts(postInfoList: List<Pair<String, String>>): List<Pair<String, List<String>>> {
        val posts = handlerAsync(postInfoList, fun(a: Any): Pair<String, List<String>> {
            val postInfo = a as Pair<String, String>
            val urls = Jsoup.connect(postInfo.second).get()
                .select("#gallery-2 .gallery-item img")
                .map { it.attr("src").replace(Regex("-\\d+x\\d+"), "") }
            log.debug("帖子解析url完成, title: ${postInfo.first}, url: ${postInfo.second}, size: ${urls.size}")
            return postInfo.first.replace(Regex("[<>?*\"|\\\\:/\\s]"), "") to urls
        })
        File("image").mkdirs()
        val file = File("image/posts.json")
        file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(posts))
        log.info("帖子解析完成, 数据已保存至文件`${file.absolutePath}`")
        return posts
    }

    @JvmStatic
    private fun getPostInfoList(): List<Pair<String, List<String>>> {
        val file = File("image/posts.json")
        return if (file.exists()) Gson().fromJson(file.readText(),
            object : TypeToken<List<Pair<String, List<String>>>>() {}.type)
        else spiderPosts(getPosts())
    }

    @JvmStatic
    private fun downloadImages(posts: List<Pair<String, List<String>>>) {
        val size = posts.sumOf { it.second.size }
        val complete = AtomicInteger(0)
        fun success(url: String) {
            complete.addAndGet(1)
            log.trace("下载成功, url: $url (${complete.get()}/$size)")
        }

        fun fail(url: String, e: Throwable) {
            complete.addAndGet(1)
            log.warn("下载失败, url: $url (${complete.get()}/$size)", e)
        }
        log.info("开始下载, size: $size")
        handlerAsync(posts, fun(a: Any): String {
            val (name, list) = a as Pair<String, List<String>>
            val dir = File("image/$name")
            dir.mkdirs()
            handlerAsync(list, fun(a: Any) {
                val url = a as String
                try {
                    File(dir, url.substring(url.lastIndexOf('/') + 1, url.length)).writeBytes(url.downloadAsImage())
                    success(url)
                } catch (ignore: Exception) {
                    try {
                        File(dir, url.substring(url.lastIndexOf('/') + 1, url.length)).writeBytes(url.downloadAsImage())
                        success(url)
                    } catch (e: Exception) {
                        fail(url, e)
                    }
                }
            }, threadCount = 10, timeout = 30L)
            return ""
        })
        log.info("获取完成")
        exitProcess(0)
    }

    /**
     * 启动时需要挂梯子, 否咋下载很慢
     *
     * -DproxyHost=localhost
     *
     * -DproxyPort=110
     */
    @JvmStatic
    fun main(args: Array<String>) {
        downloadImages(getPostInfoList())
    }
}