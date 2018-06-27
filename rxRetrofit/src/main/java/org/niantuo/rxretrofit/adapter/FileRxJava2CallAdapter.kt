package org.niantuo.rxretrofit.adapter

import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.niantuo.rxretrofit.FileResult
import org.niantuo.rxretrofit.annotation.FileDownload
import org.niantuo.rxretrofit.obervables.FileCallObservable
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

/**
 * Created by niantuo on 2017/6/15.
 * 这个难道是文件下载的适配器？
 */
class FileRxJava2CallAdapter<R>(private val responseType: Type,
                                private val scheduler: Scheduler?,
                                private val isAsync: Boolean,
                                private val isFlowable: Boolean,
                                private val isSingle: Boolean,
                                private val isMaybe: Boolean,
                                private val fileDesc: FileDownload?) : CallAdapter<R, Any> {


    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<R>): Any {
        var observable: Observable<FileResult> = FileCallObservable(call, isAsync, fileDesc)

        if (scheduler != null) {
            observable = observable.subscribeOn(scheduler)
        }

        if (isFlowable) {
            return observable.toFlowable(BackpressureStrategy.LATEST)
        }
        if (isSingle) {
            return observable.singleOrError()
        }
        if (isMaybe) {
            return observable.singleElement()
        }

        return observable
    }
}