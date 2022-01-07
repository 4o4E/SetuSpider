package com.e404.spider

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import java.io.File
import kotlin.system.exitProcess


@Suppress("UNCHECKED_CAST")
object SpiderNiJi {
    private const val url = "https://www.niji-wired.info/page/{p}/"

    // list<pair<title, url>>
    @JvmStatic
    fun getPosts(): List<Pair<String, String>> {
        return handlerAsync((1..94).map { url.replace("{p}", it.toString()) },
            fun(a: Any): Pair<String, String> {
                val s = a as String
                val d = Jsoup.connect(s)
                    .ignoreContentType(true)
                    .get().select(".listCT li > a")
                println("目录解析完成, url: $s")
                return d.attr("title") to d.attr("href")
            })
    }

    // map<title, list<url>>
    @JvmStatic
    private fun spiderPosts(posts: List<Pair<String, String>>): List<Pair<String, List<String>>> {
        return handlerAsync(posts, fun(a: Any): Pair<String, List<String>> {
            val postUrl = a as Pair<String, String>
            return postUrl.first.replace(Regex("[<>?*\"|\\\\:/\\s]"), "") to
                    Jsoup.connect(postUrl.second).get()
                        .select("#gallery-2 .gallery-item img")
                        .map { it.attr("src").replace(Regex("-\\d+x\\d+"), "") }
                        .also {
                            println("帖子解析url完成, title: ${postUrl.first}")
                        }
        })
    }

    @JvmStatic
    private fun loadFromSpider(): List<Pair<String, List<String>>> {
        val p = getPosts()
        println("=====\n获取索引完成\n=====")
        val posts = spiderPosts(p)
        println("=====\n获取url列表完成\n=====")
        return posts
    }

    @JvmStatic
    private fun loadFromJson(): List<Pair<String, List<String>>> {
        val file = File("image/image.json")
        return if (file.exists()) Gson().fromJson(file.readText(),
            object : TypeToken<List<Pair<String, List<String>>>>() {}.type)
        else loadFromSpider()
    }

    @JvmStatic
    private fun downloadImages(posts: List<Pair<String, List<String>>>) {
        File("image").mkdirs()
        File("image/image.json").writeText(GsonBuilder().setPrettyPrinting().create().toJson(posts))
        handlerAsync(posts, fun(a: Any): String {
            val (name, list) = a as Pair<String, List<String>>
            val dir = File("image/$name")
            dir.mkdirs()

            handlerAsync(list, fun(a: Any) {
                val url = a as String
                var count = 0

                @Synchronized
                fun count() = ++count
                try {
                    val bytes = url.downloadAsImage()
                    File(dir, url.substring(url.lastIndexOf('/') + 1, url.length))
                        .writeBytes(bytes)
                    println("下载成功: $url (${count()}/${list.size})")
                } catch (e: Exception) {
                    println("下载失败: $url (${count()}/${list.size})")
                    e.printStackTrace()
                }
            }, maxThreadCount = 10, timeout = 30L)

            return ""
        })
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
        val posts = loadFromJson()
        downloadImages(posts)
    }
}