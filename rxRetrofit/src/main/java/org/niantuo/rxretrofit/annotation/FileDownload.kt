package org.niantuo.rxretrofit.annotation




/**
 * 默认值，如果是该参数，将使用默认是
 */
object FileDownloadDefault {

    val default = "file-download-default"
}


/**
 * Created by niantuo on 2017/6/14.
 * 添加注解支持，只有配置该注解，才会将数据流转换成File文件
 * 值将为文件的保存位置
 * @property filePath 下载保存文件的路径
 * @property fileName 下载需要保存的文件名
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class FileDownload(
        val filePath: String = "file-download-default",
        val fileName: String = "file-download-default"

)

@Retention(AnnotationRetention.RUNTIME)
annotation class FileUpload

