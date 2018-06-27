package org.niantuo.rxretrofit.utils

import okhttp3.Request
import retrofit2.Call

/**
 * Created by niantuo on 2017/6/16.
 */
object RequestUtils {

    /***
     * Inspects an OkHttp-powered Call<T> and builds a Request
     * * @return A valid Request (that contains query parameters, right method and endpoint)
    </T> */
    fun buildRequestFromCall(call: Call<*>): Request? {
        var request: Request?
        try {
            request = call.request()
        } catch (e: NoSuchMethodError) {
            try {
                // 旧版本需要通过反射获取Request
                val argsField = call.javaClass.getDeclaredField("args")
                argsField.isAccessible = true
                val args = argsField.get(call) as Array<*>

                val requestFactoryField = call.javaClass.getDeclaredField("requestFactory")
                requestFactoryField.isAccessible = true
                val requestFactory = requestFactoryField.get(call)
                val createMethod = requestFactory.javaClass.getDeclaredMethod("create", Array<Any>::class.java)
                createMethod.isAccessible = true
                request = createMethod.invoke(requestFactory, *arrayOf<Any>(args)) as Request
            } catch (exc: Exception) {
                return null
            }
        }

        return request
    }


}