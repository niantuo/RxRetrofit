package org.niantuo.rxretrofit.annotation


enum class RxCacheDefault {
     ASYNC, SYNC
}

/**
 * Created by niantuo on 2017/6/2.
 * 这个地方按照我的需求该一下吧，
 * 1、如果有缓存，先返回缓存，等待网络返回结果，在返回结果，也就是说会返回两次结果
 * 2、如果网络获取失败，则返回缓存，只会返回一次，缓存或者网络
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class RxCache(
        /**
         * 是同步获取缓存和网络数据，还是异步进行

         * @return
         */
        val sync: RxCacheDefault = RxCacheDefault.ASYNC)
