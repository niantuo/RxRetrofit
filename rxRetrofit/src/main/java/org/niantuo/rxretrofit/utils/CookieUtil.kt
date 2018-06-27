package org.niantuo.rxretrofit.utils

import android.content.Context
import org.niantuo.rxretrofit.RxRetrofit

/**
 * Created by niantuo on 2017/6/2.
 *
 */

object CookieUtil {

    private val COOKIE_NAME = "cookie_name"
    private val KEY_COOKIE = "key_cookie"

    /**
     * 保存session ID

     * @param Cookie
     */
    fun saveCookie(Cookie: String) {
        val preferences = RxRetrofit.get().applicationContext!!.getSharedPreferences(COOKIE_NAME, Context.MODE_PRIVATE)
        preferences.edit()
                .putString(KEY_COOKIE, Cookie)
                .apply()
    }

    fun clearCookie() {
        val preferences = RxRetrofit.get().applicationContext!!.getSharedPreferences(COOKIE_NAME, Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
    }

    fun saveCookieList(list: MutableSet<String>) {
        val preferences = RxRetrofit.get().applicationContext!!.getSharedPreferences(COOKIE_NAME, Context.MODE_PRIVATE)
        if (!list.isEmpty())
            preferences.edit().putStringSet(KEY_COOKIE, list).apply()
    }

    fun getCookieList(): MutableSet<String> {
        val preferences = RxRetrofit.get().applicationContext!!.getSharedPreferences(COOKIE_NAME, Context.MODE_PRIVATE)
        return preferences.getStringSet(KEY_COOKIE, emptySet())
    }

    val cookie: String
        get() = RxRetrofit.get().applicationContext!!.getSharedPreferences(COOKIE_NAME, Context.MODE_PRIVATE)
                .getString(KEY_COOKIE, "")
}
