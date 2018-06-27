package org.niantuo.rxretrofit.adapter

import org.niantuo.rxretrofit.FileResult
import io.reactivex.*
import org.niantuo.rxretrofit.annotation.FileDownload
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by niantuo on 2017/6/15.
 * 上传下载文件文件的适配器，跟RxJava 结合起来
 */
class FileRxJava2CallAdapterFactory private constructor(val scheduler: Scheduler? = null,
                                                        val isAsync: Boolean = true) : CallAdapter.Factory() {


    /**
     * 只是文件上传的适配器，返回的类型为File类型
     */
    override fun get(returnType: Type, annotations: Array<out Annotation>?, retrofit: Retrofit): CallAdapter<Any, *>? {

        println("FileRxJava2CallAdapterFactory $returnType")

        val rawType = getRawType(returnType)

        val isFlowable = rawType == Flowable::class.java
        val isSingle = rawType == Single::class.java
        val isMaybe = rawType == Maybe::class.java
        if (rawType != Observable::class.java && !isFlowable && !isSingle && !isMaybe) {
            return null
        }

        if (returnType !is ParameterizedType) {
            val name = if (isFlowable)
                "Flowable"
            else if (isSingle)
                "Single"
            else if (isMaybe) "Maybe" else "Observable"
            throw IllegalStateException(name + " return type must be parameterized"
                    + " as " + name + "<Foo> or " + name + "<? extends Foo>")
        }

        val responseType = getParameterUpperBound(0, returnType)
        val genRawType = getRawType(responseType)
        if (genRawType != FileResult::class.java)
            return null


        var fileDesc: FileDownload? = null

        annotations.orEmpty()
                .iterator()
                .forEach { annotation ->
                    if (annotation is FileDownload) {
                        fileDesc = annotation
                    }
                    return@forEach
                }

        return FileRxJava2CallAdapter(responseType, scheduler, isAsync, isFlowable, isSingle, isMaybe,fileDesc)

    }


    /**
     * 伴生对象，相当于类里面的静态方法
     */
    companion object {

        /**
         * 默认方法，操作同步进行
         */
        fun create(): FileRxJava2CallAdapterFactory {
            return FileRxJava2CallAdapterFactory(null, false)
        }

        /**
         * 设置默认的工作线程为异步进行
         */
        fun createAsync(): FileRxJava2CallAdapterFactory {
            return FileRxJava2CallAdapterFactory(null, true)
        }

        /**
         * 设置默认的操作线程
         */
        fun createWithScheduler(scheduler: Scheduler?): FileRxJava2CallAdapterFactory {
            if (scheduler == null) throw NullPointerException("scheduler == null")
            return FileRxJava2CallAdapterFactory(scheduler, false)
        }
    }
}