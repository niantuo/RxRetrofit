package org.niantuo.rxretrofit.subscribers

import io.reactivex.functions.Consumer

/**
 * Created by niantuo on 2017/5/15.
 * RxBus 的观察者，这样是为了能够捕获异常，让RxBus 能够正常服务
 * 否则一旦出现异常，RxBus将不能正常运行
 */

class RxBusConsumer<T>(val callback: (T) -> Unit) : Consumer<T> {


    override fun accept(t: T) {
        try {
            callback.invoke(t)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
