package org.niantuo.rxretrofit

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.disposables.Disposable
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.niantuo.rxretrofit.adapter.Rx2CacheCallAdapterFactory
import org.niantuo.rxretrofit.cache.BasicCache
import org.niantuo.rxretrofit.utils.CookieUtil
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by niantuo on 2017/6/2.
 * retrofit 请求model
 * 一般文本传输
 */

open class RxRetrofitModel<S> @JvmOverloads constructor(cls: Class<S>,
                                                        baseUrl: String? = RxRetrofit.get().getBaseURL(),
                                                        timeout: Long = RxRetrofit.get().getTimeOut(),
                                                        var cookie: Boolean = true) : DisposableListener {

    companion object {
        const val TAG="RxRetrofitModel"
    }

    private val mService: S
    private val mDisposable: MutableList<Disposable>

    constructor(cls: Class<S>, timeout: Long) : this(cls, RxRetrofit.get().getBaseURL(), timeout)


    protected fun service(): S = mService


    init {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(timeout, TimeUnit.MILLISECONDS)
        builder.writeTimeout(timeout, TimeUnit.MILLISECONDS)
        builder.readTimeout(timeout, TimeUnit.MILLISECONDS)

        setOkHttpClient(builder)
        setOkHttpClientBuilder(builder)

        val retrofitBuilder = Retrofit.Builder()

        setRetrofitBuilder(retrofitBuilder)

        retrofitBuilder
                .addCallAdapterFactory(Rx2CacheCallAdapterFactory.create(BasicCache.fromCtx(RxRetrofit.get().applicationContext!!)))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())

        val gsonBuilder = GsonBuilder()
        setGsonConverterFactory(gsonBuilder)


        mService = retrofitBuilder
                .client(builder.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .baseUrl(baseUrl!!)
                .build()
                .create(cls)

        mDisposable = ArrayList()
    }

    open fun setOkHttpClient(builder: OkHttpClient.Builder) {
        /**
         * 添加headers
         */
        val addHeadersInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            if (RxRetrofit.get().isCookie() && cookie) {
                CookieUtil.getCookieList()
                        .forEach { requestBuilder.addHeader("Cookie", it) }
            }
            val request = addHeaders(requestBuilder)
            val response = chain.proceed(request)
            saveCookie(response)
            response
        }
        builder.addInterceptor(addHeadersInterceptor)

        if (RxRetrofit.get().isDebug()) {
            val loggerInterceptor = HttpLoggingInterceptor()
            setLoggerInterceptorLevel(loggerInterceptor)
            builder.addInterceptor(loggerInterceptor)
        }
    }

    /**
     * 设置日志打印级别

     * @param loggerInterceptor
     */
    protected open fun setLoggerInterceptorLevel(loggerInterceptor: HttpLoggingInterceptor) {
        loggerInterceptor.level = HttpLoggingInterceptor.Level.BODY
    }

    protected open fun addHeaders(builder: Request.Builder): Request = builder.build()

    /**
     * 保存session ID

     * @param response
     */
    protected open fun saveCookie(response: Response) {
        //存入Session
        val cookies = response.headers("Set-Cookie")
        if (cookies != null && cookies.isNotEmpty()) {
            CookieUtil.saveCookieList(cookies.toMutableSet())
        }
    }


    protected open fun setOkHttpClientBuilder(builder: OkHttpClient.Builder) {
        Log.d("RxRetrofitModel", builder.toString())
    }


    protected open fun setRetrofitBuilder(builder: Retrofit.Builder) {}

    protected open fun setGsonConverterFactory(builder: GsonBuilder) {}

    fun createJsonBody(obj: Any): RequestBody =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(obj))

    /***
     * 每次添加的时候都去检查一下，移除已经已取消的
     * @param disposable
     */
    override fun addDisposable(disposable: Disposable?) {
        if (disposable == null) return
        mDisposable.add(disposable)
    }

    override fun removeDisposable(disposable: Disposable?) {
        if (disposable == null) return
        mDisposable.remove(disposable)
    }

    /**
     * 取消任务发布,防止发生内存泄漏
     */
    fun cancel() {
        if (mDisposable.isEmpty()) return
        synchronized(mDisposable) {
            for (disposable in mDisposable) {
                disposable.dispose()
            }
        }
    }

}
