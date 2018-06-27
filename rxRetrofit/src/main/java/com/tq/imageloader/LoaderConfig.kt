package com.tq.imageloader

import android.content.Context

/**
 * Created by niantuo on 2017/6/5.
 * 默认图设置和基本缓存目录
 */
interface LoaderConfig {

    fun getContext(): Context

    fun getDefaultResId(): Int

    fun getBasePath(): String

    fun getImageHost(): String?
}