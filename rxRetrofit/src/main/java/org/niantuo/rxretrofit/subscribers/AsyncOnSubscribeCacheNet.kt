package org.niantuo.rxretrofit.subscribers


import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.niantuo.rxretrofit.annotation.RxCacheDefault
import java.util.concurrent.CountDownLatch

/**
 * 缓存+网络请求+异步进行,
 * 异步进行的概念
 * 获取缓存和向网络获取数据同时进行，如果网络请求更快，则不返回缓存结果，如果本地缓存更快，则会先返回本地缓存结果，再返回网络请求结果
 * User: tony
 */
open class AsyncOnSubscribeCacheNet<T>(protected val mCacheObservable: Observable<T>,
                                       protected val mNetObservable: Observable<T>, // 网络读取完毕,缓存动作
                                       protected val storeCacheAction: Consumer<T>,
                                       internal val sync: RxCacheDefault) : ObservableOnSubscribe<T> {


    private val TAG = AsyncOnSubscribeCacheNet::class.java.simpleName


    private val cacheLatch = CountDownLatch(1)
    private val netLatch = CountDownLatch(1)


    @Throws(Exception::class)
    override fun subscribe(emitter: ObservableEmitter<T>) {
        if (sync == RxCacheDefault.ASYNC) {
            mCacheObservable
                    .subscribeOn(Schedulers.io())
                    .subscribe(CacheObserver(emitter))
            mNetObservable
                    .subscribeOn(Schedulers.io())
                    .subscribe(NetObserver(emitter, storeCacheAction))
        } else {
            mNetObservable.subscribe(NetObserver(emitter, storeCacheAction))
        }
    }

    /**
     * 网络请求错误的时候调用该方法
     * 如果是同步请求，则去读取缓存
     */
    open fun netRequestErr(emitter: ObservableEmitter<T>) {
        if (sync == RxCacheDefault.SYNC) {
            mCacheObservable.subscribe(CacheObserver(emitter))
        }
    }


    /**
     * 网络请求的观察者
     */
    internal inner class NetObserver(private val requestSubscriber: ObservableEmitter<T>,
                                     var storeCacheAction: Consumer<T>?) : Observer<T> {

        internal var data: T? = null

        override fun onSubscribe(d: Disposable) {}

        override fun onComplete() {
            Log.i(TAG, "net onCompleted ")
            try {
                logThread("保存到本地缓存 ")
                storeCacheAction?.accept(data)

            } catch (e: Exception) {
                onError(e)
            }

            if (!requestSubscriber.isDisposed) {
                requestSubscriber.onComplete()
            }
            netLatch.countDown()
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "net onError ")
            netRequestErr(requestSubscriber)
            try {
                Log.e(TAG, "net onError await if cache not completed.")
                //会让该线程暂停
                cacheLatch.await()
                Log.e(TAG, "net onError await over.")
            } catch (e1: InterruptedException) {
                e1.printStackTrace()
            }
            if (!requestSubscriber.isDisposed) {
                requestSubscriber.onError(e)
            }
        }

        override fun onNext(o: T) {
            Log.i(TAG, "net onNext o:" + o)
            data = o
            if (!requestSubscriber.isDisposed) {
                Log.d(TAG, "onNext -> $o")
                requestSubscriber.onNext(o)
            }
        }
    }

    /**
     * 缓存任务的观察者
     */
    internal inner class CacheObserver(var subscriber: ObservableEmitter<in T>?) : Observer<T> {

        internal var cacheData: T? = null

        override fun onSubscribe(d: Disposable) {

        }

        override fun onComplete() {
            Log.i(TAG, "cache onCompleted")
            cacheLatch.countDown()
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "cache onError")
            Log.e(TAG, "read cache error:" + e.message)
            e.printStackTrace()
            cacheLatch.countDown()
        }

        override fun onNext(o: T) {
            cacheData = o
            Log.i(TAG, "cache onNext o:" + netLatch.count)
            if (netLatch.count > 0) {

                if (!(subscriber?.isDisposed ?: false)) {
                    subscriber?.onNext(o)
                }
            } else {
                Log.e(TAG, "net result had been load,so cache is not need to load")
            }
        }
    }

    fun logThread(tag: String) {
        Log.i(TAG, tag + " : " + Thread.currentThread().name)
    }

}
