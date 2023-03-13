package top.e404.managerDownloader

import org.junit.Test
import top.e404.spider.spider.CinGuru
import java.io.File

class CinGuruTest {
    @Test
    fun t1() {
        CinGuru.getImageUrls(424811, File("F:/D"))
    }
}