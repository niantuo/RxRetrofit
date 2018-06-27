package org.niantuo.rxretrofit

import android.util.Log
import com.niantuo.rxretrofit.file.FileRequestBody
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.niantuo.rxretrofit.adapter.FileRxJava2CallAdapterFactory
import org.niantuo.rxretrofit.converter.FileRxJava2ConverterFactory
import retrofit2.Retrofit
import java.io.File

/**
 * Created by niantuo on 2017/6/2.
 * 文件上传下载的工具模型
 */

val TIME_OUT: Long = 30000L

open class RxRetrofitFileModel<S>(cls: Class<S>,
                                  baseUrl: String? = RxRetrofit.get().getBaseURL(),
                                  timeout: Long = TIME_OUT) : RxRetrofitModel<S>(cls, baseUrl, timeout) {


    fun makeRequestBody(file: String): HashMap<String, RequestBody> {
        return makeRequestBodyMap("files", file)
    }

    /**
     * 创建用于文件上传的请求体

     * @param formName 表单名
     * *
     * @param file     文件
     * *
     * @return
     */
    fun makeRequestBodyMap(formName: String, file: String): HashMap<String, RequestBody> {
        val bodyMap = HashMap<String, RequestBody>()
        addBody(formName, bodyMap, file)
        return bodyMap
    }

    /**
     * 批量上传文件

     * @param formName  表单名
     * *
     * @param filePaths 文件绝对路径
     * *
     * @return
     */
    fun makeRequestBodyMap(formName: String, filePaths: List<String>): HashMap<String, RequestBody> {
        val bodyMap = HashMap<String, RequestBody>()
        for (path in filePaths) {
            addBody(formName, bodyMap, path)
        }
        return bodyMap
    }


    override fun setRetrofitBuilder(builder: Retrofit.Builder) {
        builder.addCallAdapterFactory(FileRxJava2CallAdapterFactory.createAsync())
        builder.addConverterFactory(FileRxJava2ConverterFactory.create())
    }

    override fun addHeaders(builder: Request.Builder): Request {
        builder.addHeader("Connection", "close")
        return super.addHeaders(builder)
    }

    override fun setLoggerInterceptorLevel(loggerInterceptor: HttpLoggingInterceptor) {
        loggerInterceptor.level = HttpLoggingInterceptor.Level.BODY
    }

    companion object {

        private val TAG = RxRetrofitFileModel::class.java.simpleName

        /**
         * 添加文件上传的内容body

         * @param formName 文件字段名
         * *
         * @param bodyHashMap   存放的参数
         * *
         * @param path  文件的绝对路径
         *
         * val key = "$formName\";filename=\"${file.name}"  filename和=号之间不能有空格，否则无法接收到文件
         *
         */
        fun addBody(formName: String, bodyHashMap: HashMap<String, RequestBody>, path: String) {
            val file = File(path)
            if (file.exists()) {
                val key = "$formName\";filename=\"${file.name}"    //formName + "\";filename=\"" + file.name
                val body = FileRequestBody.create(MediaType.parse("image/*"), file)
                bodyHashMap.put(key, body)
            } else {
                Log.i(TAG, "file $path is no  exist")
            }
        }
    }

}
