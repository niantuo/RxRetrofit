package org.niantuo.rxretrofit.obervables

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.niantuo.rxretrofit.FileResult
import org.niantuo.rxretrofit.annotation.FileDownload
import org.niantuo.rxretrofit.utils.FileUtils
import retrofit2.Call

/**
 * Created by niantuo on 2017/6/15.
 * 异步进行工作,
 * 主要是文件，所以必须要相应的转换器，否则异常,
 * 将返回响应的数据流保存到本地
 * 应该要在之前判断一下是否为文件数据流
 */
class FileCallObservable<T>(val originalCall: Call<T>,
                            val sync: Boolean,
                            val fileDesc: FileDownload? = null) : Observable<FileResult>() {

    override fun subscribeActual(observer: Observer<in FileResult>?) {

        if (observer == null) return
        val call = originalCall.clone()
        if (sync) {
            requestFileResult(call, observer, fileDesc)
        } else {
            Schedulers.io().scheduleDirect {
                requestFileResult(call, observer, fileDesc)
            }
        }
    }


    /**
     * 执行网络请求，并将返回的文件保存到本地
     *
     */
    fun requestFileResult(call: Call<T>, observer: Observer<in FileResult>, fileDesc: FileDownload?) {

        observer.onSubscribe(CallDisposable(call))
        if (call.isCanceled) {
            return
        }

        try {
            val fileResult = FileResult()
            val response = call.execute()

            val url = response.raw().request().url().toString()

            val responseBody = response.body()
            if (response.isSuccessful && responseBody is ResponseBody) {
                val file = FileUtils.writeResponseBodyToDisk(responseBody, url, fileDesc)
                fileResult.data = file
                if (fileResult.data == null) {
                    fileResult.success = false
                    fileResult.message = "保存文件出错"
                }
            } else {
                fileResult.success = false
                fileResult.message = response.message()
            }

            if (call.isCanceled) {
                return
            }
            observer.run {
                onNext(fileResult)
                onComplete()
            }
        } catch (e: Exception) {
            if (!call.isCanceled) {
                observer.onError(e)
            }
        }


    }


    /**
     * 实现取消操作的接口
     */
    private class CallDisposable internal constructor(private val call: Call<*>) : Disposable {

        override fun dispose() {
            call.cancel()
        }

        override fun isDisposed(): Boolean {
            return call.isCanceled
        }
    }
}