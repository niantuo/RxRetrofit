package org.niantuo.rxretrofit.adapter

import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.functions.Consumer
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import org.niantuo.rxretrofit.annotation.RxCacheDefault
import org.niantuo.rxretrofit.cache.IRxCache
import org.niantuo.rxretrofit.obervables.CacheNetObservable
import org.niantuo.rxretrofit.subscribers.AsyncOnSubscribeCacheNet
import org.niantuo.rxretrofit.utils.RequestUtils
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

/**
 * Created by niantuo on 2017/6/16.
 * 对Retrofit添加缓存支持的适配器，采用注解的方式
 */
class Rx2CacheCallAdapter<R>(val responseType: Type,
                             val annotations: Array<out Annotation>,
                             val retrofit: Retrofit,
                             val cachingSystem: IRxCache,
                             val sync: RxCacheDefault) : CallAdapter<R, Any> {


    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<R>): Any? {
        //获得请求体
        val request = RequestUtils.buildRequestFromCall(call)

        val mCacheObservable = Observable.create(ObservableOnSubscribe<R> { emitter ->
            val responseConverter = getResponseConverter<R>(retrofit, responseType, annotations)
            val serverResult = getFromCache(request, responseConverter, cachingSystem)

            Log.d(TAG, "cacheBody:$serverResult  isDisposed: ${emitter.isDisposed}")

            if (emitter.isDisposed) return@ObservableOnSubscribe
            if (serverResult != null) {
                emitter.onNext(serverResult)
            }
            emitter.onComplete()
        })


        val mNetObservable = CacheNetObservable(call)

        val cacheConsumer = Consumer<R> { r ->

            Log.d(TAG, "cacheConsumer -> $r")
            if (r != null) {
                val requestConverter = getRequestConverter<R>(retrofit, responseType, annotations)
                addToCache(request, r, requestConverter, cachingSystem)
            }
        }

        return Observable.create(AsyncOnSubscribeCacheNet(mCacheObservable, mNetObservable, cacheConsumer, sync))

    }


    fun <T> getFromCache(request: Request?, converter: Converter<ResponseBody, T>, cachingSystem: IRxCache): T? {
        if (request == null) return null
        val cacheResponseBody = cachingSystem.getFromCache(request) ?: return null
        try {
            return converter.convert(cacheResponseBody)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    companion object {

        private val TAG = Rx2CacheCallAdapter::class.java.simpleName

        fun <T> getResponseConverter(retrofit: Retrofit, dataType: Type, annotations: Array<out Annotation>): Converter<ResponseBody, T> {
            return retrofit.responseBodyConverter<T>(dataType, annotations)
        }

        fun <T> getRequestConverter(retrofit: Retrofit, dataType: Type, annotations: Array<out Annotation>): Converter<T, RequestBody> {
            return retrofit.requestBodyConverter<T>(dataType, arrayOfNulls<Annotation>(0), annotations)
        }

        fun <T> addToCache(request: Request?, data: T, converter: Converter<T, RequestBody>, cachingSystem: IRxCache) {

            Log.d(TAG, "添加缓存：$data")
            if (request == null) return
            try {
                val buffer = Buffer()
                val requestBody = converter.convert(data)
                requestBody.writeTo(buffer)
                cachingSystem.addInCache(request, buffer)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }
}