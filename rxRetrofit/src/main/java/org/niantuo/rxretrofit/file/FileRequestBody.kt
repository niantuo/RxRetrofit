package com.niantuo.rxretrofit.file

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.*
import org.niantuo.rxretrofit.FileProgress
import org.niantuo.rxretrofit.RxBus
import java.io.File
import java.io.IOException

/**
 * 自定义文件上传RequestBody
 * Created by：hcs on 2016/10/20 10:32
 * e_mail：aaron1539@163.com
 */
class FileRequestBody private constructor(val contentType: MediaType?,
                                          val file: File) : RequestBody() {

    /**
     * 包装完成的BufferedSink
     */
    private var bufferedSink: BufferedSink? = null

    private val mProgress: FileProgress

    init {
        val path = file.absolutePath
        mProgress = FileProgress(path, contentLength(), path, 0L)
    }


    companion object {
        fun create(contentType: MediaType?, file: File): RequestBody {
            return FileRequestBody(contentType, file)
        }

    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return file.length()
    }

    override fun contentType(): MediaType? {
        return contentType
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {

        //包装
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink))
        }

        //写入
        var source: Source? = null
        try {
            source = Okio.source(file)
            if (source == null) {

            } else {
                sink.writeAll(source)
            }
        } finally {
            Util.closeQuietly(source)
        }
    }

    /**
     * 写入，回调进度接口
     * @param sink 源sink
     * @return 返回一个转发的sink
     */
    private fun sink(sink: BufferedSink): Sink {
        return object : ForwardingSink(sink) {
            //当前写入字节数
            internal var bytesWritten = 0L

            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                //增加当前写入的字节数
                bytesWritten += byteCount
                mProgress.progress = bytesWritten
                RxBus.getBus().post(mProgress)
            }
        }
    }


}
