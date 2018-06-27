package org.niantuo.rxretrofit.subscribers

import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Created by niantuo on 2017/4/21.
 * 错误问题解析，防止出现bug提示，不友好
 */

object ErrorParse {


    fun parseThrowable(throwable: Throwable?): String {
        if (throwable == null) return Err.UNKNOWN_ERR
        if (throwable is UnknownHostException) {
            return Err.NET_ERR
        } else if (throwable is JsonSyntaxException || throwable is MalformedJsonException) {
            return Err.SYS_ERR
        } else if (throwable is SocketTimeoutException) {
            return Err.TIMEOUT_ERR
        } else {
            val errMsg = throwable.message
            if (errMsg.isNullOrEmpty()) {
                return Err.UNKNOWN_ERR
            } else if (errMsg!!.contains("500")) {
                return Err.SERVER_ERR
            } else {
                return errMsg
            }
        }
    }

    object Err {

        val SYS_CODE = -1000
        val UNKNOWN_CODE = -999
        val TIMEOUT = -998

        val SYS_ERR = "系统错误。"
        val NET_ERR = "网络错误，请检查网络设置。"
        val SERVER_ERR = "服务器错误。"
        val UNKNOWN_ERR = "未知错误。"
        val TIMEOUT_ERR = "连接超时，请重试。"
    }

}
