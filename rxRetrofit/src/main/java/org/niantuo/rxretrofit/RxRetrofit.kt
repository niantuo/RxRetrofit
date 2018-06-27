package org.niantuo.rxretrofit

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * Created by niantuo on 2017/6/2.
 * Retrofit 的配置文件
 */

class RxRetrofit {

    private var mBaseURL: String? = null
    private var mTimeOut: Long = 5000
    private var debug = false
    private var cookie = true
    var fileRoot = "RxRetrofit"

    var applicationContext: Context? = null
        private set

    fun setContext(application: Application): RxRetrofit {
        applicationContext = application
        return this
    }

    /**
     * 设置基本的URL

     * @param mBaseURL
     * *
     * @return
     */
    fun setBaseURL(mBaseURL: String): RxRetrofit {
        this.mBaseURL = mBaseURL
        return this
    }

    fun getBaseURL(): String? = mBaseURL


    fun setFileRoot(fileName: String): RxRetrofit {
        this.fileRoot = fileName
        return this
    }

    /**
     * 设置超时时间

     * @param mTimeOut
     * *
     * @return
     */
    fun setTimeOut(mTimeOut: Long): RxRetrofit {
        this.mTimeOut = mTimeOut
        return this
    }

    fun getTimeOut(): Long = mTimeOut

    fun setDebug(debug: Boolean): RxRetrofit {
        this.debug = debug
        return this
    }

    fun isDebug(): Boolean = debug

    fun setCookie(cookie: Boolean): RxRetrofit {
        this.cookie = cookie
        return this
    }

    fun isCookie(): Boolean = cookie

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var rxRetrofit: RxRetrofit? = null
        fun get(): RxRetrofit {
            if (rxRetrofit == null) {
                synchronized(RxRetrofit::class.java) {
                    if (rxRetrofit == null) {
                        rxRetrofit = RxRetrofit()
                    }
                }
            }
            return rxRetrofit!!
        }
    }
}
