package org.niantuo.rxretrofit.cache

import okhttp3.Request
import okhttp3.ResponseBody
import okio.Buffer

/**
 * Created by niantuo on 2017/6/2.
 * 缓存接口
 */

interface IRxCache {

    fun addInCache(request: Request, buffer: Buffer)

    fun getFromCache(request: Request): ResponseBody?

}
