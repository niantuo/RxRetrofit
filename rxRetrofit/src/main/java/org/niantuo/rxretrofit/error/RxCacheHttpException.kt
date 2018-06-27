package org.niantuo.rxretrofit.error

import retrofit2.Response

/** Exception for an unexpected, non-2xx HTTP response.  */
class RxCacheHttpException(@Transient private val response: Response<*>) : Exception("HTTP " + response.code() + " " + response.message()) {
    private val code: Int = response.code()
    private val cacheMessage: String = response.message()

    /** HTTP status code.  */
    fun code(): Int {
        return code
    }

    /** HTTP status message.  */
    fun message(): String {
        return cacheMessage
    }

    /**
     * The full HTTP response. This may be null if the exception was serialized.
     */
    fun response(): Response<*> {
        return response
    }
}
