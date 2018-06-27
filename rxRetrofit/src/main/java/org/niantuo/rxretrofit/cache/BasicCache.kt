package org.niantuo.rxretrofit.cache

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.jakewharton.disklrucache.DiskLruCache
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Buffer
import org.niantuo.rxretrofit.utils.MD5
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

/**
 * Created by niantuo on 2017/6/2.
 * A basic caching system that stores responses in RAM and disk
 * It uses [com.jakewharton.disklrucache.DiskLruCache] and [android.util.LruCache] to do the former.
 */

class BasicCache(diskDirectory: File, maxDiskSize: Long, memoryEntries: Int) : IRxCache {


    private var diskCache: DiskLruCache? = null
    private val memoryCache: LruCache<String, Any>

    init {
        try {
            diskCache = DiskLruCache.open(diskDirectory, 1, 1, maxDiskSize)
        } catch (exc: IOException) {
            Log.e("BasicCache", "", exc)
            diskCache = null
        }

        memoryCache = LruCache<String, Any>(memoryEntries)
    }

    override fun addInCache(request: Request, buffer: Buffer) {
        val rawResponse = buffer.readByteArray()
        val cacheKey = urlToKey(request.url())
        memoryCache.put(cacheKey, rawResponse)
        try {
            val editor = diskCache!!.edit(urlToKey(request.url()))
            editor.set(0, String(rawResponse, Charset.defaultCharset()))
            editor.commit()
        } catch (exc: IOException) {
            Log.e("BasicCache", "", exc)
        }

    }

    override fun getFromCache(request: Request): ResponseBody? {
        val cacheKey = urlToKey(request.url())
        val memoryResponse = memoryCache.get(cacheKey) as? ByteArray
        if (memoryResponse != null) {
            Log.d("BasicCache", "Memory hit!")
            return ResponseBody.create(null, memoryResponse)
        }

        try {
            val cacheSnapshot = diskCache!!.get(cacheKey)
            if (cacheSnapshot != null) {
                Log.d("BasicCache", "Disk hit!")
                return ResponseBody.create(null, cacheSnapshot.getString(0).toByteArray())
            } else {
                return null
            }
        } catch (exc: IOException) {
            return null
        }

    }

    private fun urlToKey(url: HttpUrl): String {
        return MD5.getMD5(url.toString())
    }

    companion object {

        private val REASONABLE_DISK_SIZE = (1024 * 1024).toLong() // 1 MB
        private val REASONABLE_MEM_ENTRIES = 50 // 50 entries

        /***
         * Constructs a BasicCaching system using settings that should work for everyone

         * @param context    上下文
         * *
         * @return BasicCache
         */
        fun fromCtx(context: Context): BasicCache {
            return BasicCache(
                    File(context.cacheDir, "retrofit_rxCache"),
                    REASONABLE_DISK_SIZE,
                    REASONABLE_MEM_ENTRIES)
        }
    }

}
