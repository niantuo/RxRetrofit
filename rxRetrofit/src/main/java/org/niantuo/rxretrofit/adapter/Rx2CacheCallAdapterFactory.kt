package org.niantuo.rxretrofit.adapter

import android.util.Log
import io.reactivex.Observable
import org.niantuo.rxretrofit.annotation.RxCache
import org.niantuo.rxretrofit.cache.IRxCache
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by niantuo on 2017/6/2.
 * 网络的请求是通过该适配器进行的，调用是在这里面进行的
 */

class Rx2CacheCallAdapterFactory(private val cachingSystem: IRxCache) : CallAdapter.Factory() {
    private val TAG = Rx2CacheCallAdapterFactory::class.java.simpleName

    override fun get(returnType: Type?, annotations: kotlin.Array<out Annotation>, retrofit: Retrofit): CallAdapter<Any, *>? {
        val rawType = getRawType(returnType)
        if (rawType != Observable::class.java) {
            Log.d(TAG, "get: rawType != Observable.class")
            return null
        }

        if (returnType !is ParameterizedType) {
            val name = "Observable"
            throw IllegalStateException(name + " return type must be parameterized"
                    + " as " + name + "<Foo> or " + name + "<? extends Foo>")
        }

        val responseType = getParameterUpperBound(0, returnType)

        for (annotation in annotations) {
            if (annotation is RxCache) {
                return Rx2CacheCallAdapter(responseType, annotations, retrofit, cachingSystem, annotation.sync)
            }
        }

        return null

    }


    companion object {

        fun create(cachingSystem: IRxCache): Rx2CacheCallAdapterFactory {
            return Rx2CacheCallAdapterFactory(cachingSystem)
        }

    }
}
