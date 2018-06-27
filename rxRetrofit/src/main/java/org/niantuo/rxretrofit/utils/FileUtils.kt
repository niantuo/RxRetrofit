package org.niantuo.rxretrofit.utils

import android.os.Environment
import okhttp3.ResponseBody
import org.niantuo.rxretrofit.FileProgress
import org.niantuo.rxretrofit.RxBus
import org.niantuo.rxretrofit.RxRetrofit
import org.niantuo.rxretrofit.annotation.FileDownload
import org.niantuo.rxretrofit.annotation.FileDownloadDefault
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by niantuo on 2017/6/14.
 * 文件工具类
 */
object FileUtils {

    /**
     * 将文件写入本地
     * @param body http响应体
     * @param fileDesc 保存的目录和文件名称，不加后缀，所以需要设置 如果没有设置，则默认为URL的链接附带的地址
     * @param url 下载的链接
     * *
     * @return 保存file
     */
    fun writeResponseBodyToDisk(body: ResponseBody?, url: String, fileDesc: FileDownload? = null): File? {

        if (body == null) {
            return null
        }

        var fileParent: String = RxRetrofit.get().fileRoot
        if (fileDesc != null) {
            fileParent += "/${fileDesc.filePath}/"
        }

        if (fileParent.isNullOrEmpty() || fileParent == FileDownloadDefault.default) {
            fileParent = "RxRetrofit"
        }
        val rootFile = File(Environment.getExternalStorageDirectory().absolutePath + "/$fileParent")
        if (!rootFile.exists()) {
            rootFile.mkdirs()
        }

        //文件名
        var fileName = fileDesc?.fileName
        if (fileName.isNullOrEmpty() || fileName == FileDownloadDefault.default) {
            fileName = FileUtils.fileName(url)
        } else {
            fileName += suffix(url)
        }

        val total = body.contentLength()
        val file = File(rootFile, fileName)

        RxBus.getBus().post(FileProgress(url, total, file.absolutePath))

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            var progress: Long = 0
            val fileReader = ByteArray(4096)
            inputStream = body.byteStream()
            outputStream = FileOutputStream(file)

            while (true) {
                val read = inputStream!!.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
                progress += read
                RxBus.getBus().post(FileProgress(url, total, file.absolutePath, progress))
            }

            outputStream.flush()
            return file
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     *
     * 获取URL的后缀
     */
    fun suffix(url: String): String {
        val index = url.lastIndexOf("")
        return url.substring(index + 1, url.length)
    }

    /**
     * 获取URL的文件名，包括后缀
     */
    fun fileName(url: String): String {
        val index = url.lastIndexOf("/")
        return url.substring(index + 1, url.length)
    }

}