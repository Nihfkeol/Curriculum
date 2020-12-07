package com.nihfkeol.curriculum.utils

import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

class FileUtils {
    private var _fileUtils: FileUtils? = null

    fun getInstance(): FileUtils? {
        if (_fileUtils == null) {
            synchronized(FileUtils::class.java) {
                if (_fileUtils == null) {
                    _fileUtils = FileUtils()
                }
            }
        }
        return _fileUtils
    }


    /**
     * 写入课程表数据
     * @param html 课程表内容
     */
    fun writeHtml(html: String, filePath: File) {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            try {
                val fos = FileOutputStream(filePath.toString())
                val bytes = html.toByteArray()
                fos.write(bytes)
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 读取本地储存的课程表数据
     * @return
     */
    fun readHtml(filePath: File): String? {
        try {
            val fis = FileInputStream(filePath)
            val bytes = ByteArray(fis.available())
            fis.read(bytes)
            fis.close()
            return String(bytes, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}