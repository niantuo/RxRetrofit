package org.niantuo.rxretrofit

import java.io.File

/**
 * Created by niantuo on 2017/6/15.
 * 返回值类型定义
 */
/**
 * 下载文件的返回值
 */
data class FileResult(var success: Boolean = true,
                      var message: String = "success",
                      var data: File? = null)

/**
 * 文件下载的 进度显示
 */
data class FileProgress(val url: String?,
                        val total: Long,
                        val path: String,
                        var progress: Long? = 0)