package com.tq.imageloader

import android.graphics.Bitmap
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.assist.ImageSize
import com.nostra13.universalimageloader.core.assist.QueueProcessingType
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import org.niantuo.rxretrofit.R
import java.io.File

open class DefImageLoader private constructor() {

    private val TAG = DefImageLoader::class.java.simpleName
    var imageHost: String? = null
    var mBasePath: String = Environment.getExternalStorageDirectory().absolutePath + "/ImageLoader/"

    private var mDefaultImagePath: String = mBasePath + "Images/"
        get() = mBasePath + "Images/"

    var mDefaultImageReceivedPath = mBasePath + "Save"
        get() = mBasePath + "Save/"
    var mDefaultImageResId: Int = R.drawable.ic_default_image


    fun cancel(imageView: ImageView?) {
        if (imageView == null) return
        ImageLoader.getInstance().cancelDisplayTask(imageView)
    }

    fun display(uri: String?, imageView: ImageView) {
        if (TextUtils.isEmpty(uri)) {
            imageView.setImageResource(mDefaultImageResId)
            return
        }
        ImageLoader.getInstance().displayImage(check(uri), imageView,
                noCacheInMemory(mDefaultImageResId))
    }

    fun display(uri: String?, iv: ImageView, listener: ImageLoadingListener) {
        if (TextUtils.isEmpty(uri)) {
            listener.onLoadingFailed(uri, null, FailReason(FailReason.FailType.UNKNOWN, Throwable("url is empty or null")))
            return
        }
        ImageLoader.getInstance().displayImage(check(uri), iv, listener)
    }


    fun displayLarge(uri: String?, iv: ImageView, listener: ImageLoadingListener) {
        if (TextUtils.isEmpty(uri)) {
            listener.onLoadingFailed(uri, null, FailReason(FailReason.FailType.UNKNOWN, Throwable("url is empty or null")))
            return
        }
        ImageLoader.getInstance().displayImage(check(uri), iv,
                getDefaultImageOptions(mDefaultImageResId), listener)
    }


    /**
     * 加载指定尺寸大小的图片，
     * 限制大小的

     * @param uri
     * *
     * @param iv
     * *
     * @param width
     * *
     * @param height
     */
    fun display(uri: String?, iv: ImageView, width: Int, height: Int) {
        if (TextUtils.isEmpty(uri)) {
            iv.setImageResource(mDefaultImageResId)
            return
        }
        ImageLoader.getInstance().displayImage(check(uri), iv, ImageSize(width, height))
    }


    /**
     *  @param uri 图片文件地址
     *  @param imageView 加载图片的view
     */
    fun display(uri: String?, imageView: ImageView, defaultRes: Int, listener: ImageLoadingListener) {
        if (TextUtils.isEmpty(uri)) {
            imageView.setImageResource(defaultRes)
            return
        }
        ImageLoader.getInstance().displayImage(check(uri), imageView, getDefaultImageOptions(defaultRes), listener)
    }

    fun display(imageView: ImageView?, url: String?, resId: Int) {
        if (imageView==null)return

        if (TextUtils.isEmpty(url)) {
            imageView.setImageResource(resId)
            return
        }
        ImageLoader.getInstance().displayImage(check(url), imageView, getDefaultImageOptions(resId))
    }


    fun display(imageView: ImageView, url: String?) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageResource(mDefaultImageResId)
            return
        }
        ImageLoader.getInstance().displayImage(check(url), imageView,
                getDefaultImageOptions(mDefaultImageResId))
    }

    /**
     * 异步获取bitmap

     * @param url
     * *
     * @param listener
     */
    fun loadBitmap(url: String?, listener: ImageLoadingListener) {
        if (TextUtils.isEmpty(url)) {
            listener.onLoadingFailed(url, null, FailReason(FailReason.FailType.UNKNOWN, Throwable("url is empty or null")))
            return
        }
        ImageLoader.getInstance().loadImage(check(url), getDefaultImageOptions(mDefaultImageResId), listener)
    }

    /**
     * 同步获取bitmap，并指定大小喽，不准确，但是或有压缩

     * @param url
     * *
     * @param maxSize
     * *
     * @return
     */
    fun loadBitmapSync(url: String?, maxSize: Int): Bitmap? {
        if (url.isNullOrEmpty()) {
            return null
        }
        return ImageLoader
                .getInstance()
                .loadImageSync(check(url), ImageSize(maxSize, maxSize), imageOptionsNoCache)
    }

    /**
     * 获取缓存文件

     * @param file
     * *
     * @return
     */
    fun displayCacheFile(file: String): File {
        return ImageLoader
                .getInstance()
                .diskCache
                .get(file)
    }

    /**
     * 初始化Imageloader

     * @param app
     */
    fun init(loaderConfig: LoaderConfig) {

        this.mBasePath = loaderConfig.getBasePath()
        imageHost = loaderConfig.getImageHost()
        mDefaultImageResId = loaderConfig.getDefaultResId()

        try {
            val config = ImageLoaderConfiguration.Builder(loaderConfig.getContext().applicationContext)
                    .defaultDisplayImageOptions(getDefaultImageOptions(mDefaultImageResId))
                    .threadPriority(Thread.MIN_PRIORITY)
                    .memoryCache(WeakMemoryCache())
                    .memoryCacheSize(30 * 1024 * 1024)
                    // 使用10% 的内存做缓存
                    .memoryCacheSizePercentage(10)
                    .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                    .denyCacheImageMultipleSizesInMemory()
                    .diskCache(UnlimitedDiskCache(File(mDefaultImagePath)))
                    /* .diskCacheFileNameGenerator(new Md5FileNameGenerator())*/
                    .diskCacheSize(50 * 1024 * 1024)
                    .diskCacheFileCount(100)
                    .threadPoolSize(3)
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .build()
            ImageLoader.getInstance().init(config)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 默认加载图片配置，
     * 缓存在本地和内存中

     * @param res
     * *
     * @return
     */
    private fun getDefaultImageOptions(res: Int): DisplayImageOptions {
        return DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .showImageForEmptyUri(res)
                .showImageOnFail(res)
                .showImageOnLoading(res)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build()
    }

    /**
     * 不缓存到没存中，在本地创建缓存文件

     * @return
     */
    private fun noCacheInMemory(res: Int): DisplayImageOptions {
        return DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .showImageOnFail(res)
                .showImageForEmptyUri(res)
                .showImageOnLoading(res)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build()
    }

    /**
     * 既不缓存到本地，也不缓存在内存中

     * @param res
     * *
     * @return
     */
    protected fun getDefaultImageOptionsNoCache(res: Int): DisplayImageOptions {
        return DisplayImageOptions.Builder()
                .cacheOnDisk(false)
                .cacheInMemory(false)
                .showImageOnLoading(res)
                .showImageForEmptyUri(res)
                .showImageOnFail(res)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build()
    }

    /**
     * 既不缓存到本地，也不缓存在内存中
     * 也没有默认图

     * @return
     */
    protected val imageOptionsNoCache: DisplayImageOptions
        get() = DisplayImageOptions.Builder()
                .cacheOnDisk(false)
                .cacheInMemory(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build()

    /**
     * 清除本地缓存
     */
    fun clearCache() {
        ImageLoader.getInstance().clearDiskCache()
        FileUtils.removeDirectory(ImageLoader.getInstance().diskCache.directory)
    }

    /**
     * 获取的当前缓存大小

     * @return
     */
    val cacheSize: Long
        get() {
            val cacheDir = ImageLoader.getInstance().diskCache.directory
            if (cacheDir == null || cacheDir.isFile) {
                return 0
            }
            val size = FileUtils.getFolderSize(cacheDir)
            return size
        }


    companion object {
        val default: DefImageLoader
            get() {
                return Holder.loader
            }
        fun getPath(): String {
            return default.mBasePath
        }

        /**
         * 返回一个正确的URL地址，如果没有设置图片主机，则返回原样

         * @param url
         * *
         * @return
         */
        fun check(url: String?): String {
            var resultUrl: String = url ?: ""
            if (default.imageHost.isNullOrEmpty()
                    || url.isNullOrEmpty() || url!!.startsWith("drawable://")) {
            } else {
                if (!resultUrl.startsWith("file") && !resultUrl.startsWith("http")) {
                    resultUrl = default.imageHost + url
                }
            }
            Log.d(default.TAG, "URL->" + resultUrl)
            return resultUrl
        }
    }


    private object Holder {
        val loader = DefImageLoader()
    }

}
