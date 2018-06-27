package org.niantuo.rxretrofit.subscribers

import android.util.Log
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.niantuo.rxretrofit.DisposableListener
import org.niantuo.rxretrofit.ResultListener
import org.niantuo.rxretrofit.SaveListener

/**
 * Created by niantuo on 2017/2/26.
 * 回调函数
 */

open class RxObserver<T>(private var disposableListener: DisposableListener? = null,
                         private val listener: ResultListener<T>? = null,
                         private val requestCode: Int = 11,
                         private var saveListener: SaveListener<T>? = null) : Observer<T> {

    companion object {
        internal val TAG = RxObserver::class.java.simpleName
        fun <T> create(disposableListener: DisposableListener,
                       listener: ResultListener<T>? = null,
                       requestCode: Int = 11): RxObserver<T> =
                RxObserver(disposableListener, listener, requestCode)

    }

    var saveFun = fun(_: Int, _: T) {}
    var successFun = fun(_: Int, _: T) {}
    var errorFun = fun(_: Int, _: String) {}


    fun setSaveListener(listener: SaveListener<T>): RxObserver<T> {
        saveListener = listener
        return this
    }

    fun addSaveListener(listener: (requestCode: Int, data: T) -> Unit): RxObserver<T> {
        saveFun = listener
        return this
    }

    fun addSuccessListener(listener: (requestCode: Int, data: T) -> Unit): RxObserver<T> {
        successFun = listener
        return this
    }

    fun addErrorListener(listener: (requestCode: Int, message: String) -> Unit): RxObserver<T> {
        errorFun = listener
        return this
    }


    private var disposable: Disposable? = null


    override fun onNext(t: T) {
        if (t == null) {
            AndroidSchedulers.mainThread()
                    .scheduleDirect {
                        listener?.onError(requestCode, ErrorParse.Err.SYS_ERR)
                        errorFun.invoke(requestCode, ErrorParse.Err.SYS_ERR)
                    }
            return
        }

        onSave(requestCode, t)
        AndroidSchedulers.mainThread()
                .scheduleDirect {
                    listener?.onSuccess(requestCode, t)
                    successFun.invoke(requestCode, t)
                }

    }

    override fun onComplete() {
        disposableListener?.removeDisposable(disposable!!)
    }

    override fun onError(throwable: Throwable) {
        disposableListener?.removeDisposable(disposable!!)
        Log.e(TAG, "onError: ", throwable)
        val msg = ErrorParse.parseThrowable(throwable)
        AndroidSchedulers.mainThread()
                .scheduleDirect {
                    listener?.onError(requestCode, msg)
                    errorFun.invoke(requestCode, msg)
                }
    }

    override fun onSubscribe(d: Disposable) {
        this.disposable = d
        disposableListener?.addDisposable(d)
    }

    protected open fun onSave(requestCode: Int, data: T) {
        saveFun.invoke(requestCode, data)

    }
}
