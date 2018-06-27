package org.niantuo.rxretrofit.subscribers

import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Created by niantuo on 2017/5/15.
 * RxBus 的观察者，这样是为了能够捕获异常，让RxBus 能够正常服务
 * 否则一旦出现异常，RxBus将不能正常运行
 */

abstract class RxBusSubscriber<T> : Subscriber<T> {


    override fun onSubscribe(s: Subscription) {

    }

    override fun onError(t: Throwable) {

    }

    override fun onComplete() {

    }

    override fun onNext(t: T) {
        try {
            onEvent(t)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    protected abstract fun onEvent(t: T)
}
