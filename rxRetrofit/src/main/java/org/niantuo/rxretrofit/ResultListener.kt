package org.niantuo.rxretrofit

/**
 * Created by niantuo on 2017/4/22.
 * 回调函数啊
 */

interface ResultListener<in T> {

    fun onSuccess(requestCode: Int, data: T)

    fun onError(requestCode: Int, errMsg: String)

}


interface SaveListener<in T> {


    fun onSave(requestCode: Int, data: T)

}

