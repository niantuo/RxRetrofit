package org.niantuo.rxretrofit.obervables

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.niantuo.rxretrofit.error.RxCacheHttpException
import retrofit2.Call
import retrofit2.Response

/**
 * Created by niantuo on 2017/6/16.
 */
class CacheNetObservable<R>(val originalCall: Call<R>) : Observable<R>() {

    internal val TAG = CacheNetObservable::class.java.simpleName

    override fun subscribeActual(observer: Observer<in R>?) {
        //设置取消请求回调
        val call = originalCall.clone()
        observer?.onSubscribe(CallDisposable(call))

        val response: Response<R>

        try {
            response = call.execute()
        } catch (e: Exception) {
            e.printStackTrace()
            if (!call.isCanceled)
                observer?.onError(e)
            return
        }

        var success: Boolean = false
        try {
            success = response?.isSuccessful ?: false
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            try {
                val isSuccess = response?.javaClass?.getDeclaredMethod("isSuccess")
                success = isSuccess?.invoke(response) as Boolean
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (call.isCanceled) {
            Log.d(TAG, "call has canceled ")
            return
        }

        Log.d(TAG, "response is succeed ")

        if (success) {
            observer?.run {
                onNext(response.body()!!)
                onComplete()
            }
        } else {
            observer?.onError(RxCacheHttpException(response))
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