package org.niantuo.rxretrofit

import io.reactivex.disposables.Disposable

/**
 * Created by niantuo on 2017/6/2.
 * 定于的监听器
 */

interface DisposableListener {

    fun addDisposable(disposable: Disposable?)
    fun removeDisposable(disposable: Disposable?)
}
