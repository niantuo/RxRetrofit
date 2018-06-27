package org.niantuo.rxretrofit.converter

import org.niantuo.rxretrofit.FileResult
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.niantuo.rxretrofit.annotation.FileUpload
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type


/**
 * Created by niantuo on 2017/6/15.
 * 这个是 FileResult 文件下载的转换器，什么工作也没有做，原样返回ResponseBody
 */
class FileRxJava2ConverterFactory : Converter.Factory() {


    companion object {
        fun create(): Converter.Factory {
            return FileRxJava2ConverterFactory()
        }
    }

    /**
     * 下载文件的转换器
     */
    override fun responseBodyConverter(type: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
        if (type == FileResult::class.java) {
            return FileRxJava2Converter()
        }
        return null
    }


    /**
     * 上传文件的转换器
     * RxJava 根本不走这个方法，所以这儿写是没有用的
     */
    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<out Annotation>?,
                                      methodAnnotations: Array<out Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody>? {

        println("FileRxJava2ConverterFactory -> requestBodyConverter : $type")


        methodAnnotations.orEmpty()
                .iterator()
                .forEach {
                    println("methodAnnotations - > $it")
                }

        parameterAnnotations.orEmpty()
                .iterator()
                .forEach {
                    println("methodAnnotations - > $it")
                }

        for (annotation in parameterAnnotations.orEmpty().iterator()) {

            println(" annotation -> $annotation")
            if (annotation is FileUpload) {
                return FileRxJava2RequestConverter()
            }
        }

        return null
    }


    /**
     * 文件上传转换器，这个地方需要添加上传进度显示
     */
    class FileRxJava2RequestConverter : Converter<Any, RequestBody> {
        override fun convert(value: Any?): RequestBody {
            println("FileRxJava2RequestConverter  ->  $value")

            return value as RequestBody
        }
    }


    class FileRxJava2Converter : Converter<okhttp3.ResponseBody, Any> {
        override fun convert(value: ResponseBody): Any {
            println("FileRxJava2Converter - > $value")
            return value
        }
    }
}