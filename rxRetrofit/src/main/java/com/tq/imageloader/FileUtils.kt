package com.tq.imageloader

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件操作类
 * Created by Nereo on 2015/4/8.
 */
object FileUtils {

    private val cacheBaseDirName = "core"
    private val CACHE_DIR = "images"


    /**
     * 获取Imageloader缓存的文件

     * @param context
     * *
     * @param cacheDirName
     * *
     * @param cacheFileName
     * *
     * @return
     */
    fun getCacheFile(context: Context,
                     cacheDirName: String, cacheFileName: String?): File? {
        var cacheFile: File? = null
        val cacheDir = getCacheDir(context, cacheDirName)
        if (null != cacheDir && null != cacheFileName
                && cacheFileName.trim { it <= ' ' }.isNotEmpty()) {
            cacheFile = File(cacheDir, cacheFileName)
        }
        return cacheFile
    }

    /**
     * 获取缓存目录

     * @param context
     * *
     * @param cacheDirName
     * *
     * @return
     */
    private fun getCacheDir(context: Context,
                            cacheDirName: String?): File? {
        var cacheDir: File? = null
        val cacheBaseDir = getCacheBaseDir(context)

        if (null != cacheBaseDir) {
            if (null != cacheDirName && cacheDirName.trim { it <= ' ' }.isNotEmpty()) {
                cacheDir = File(cacheBaseDir, cacheDirName)
            } else {
                cacheDir = cacheBaseDir
            }
        }

        if (null != cacheDir && !cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                cacheDir = null
            }
        }

        if (null == cacheDir) {
            cacheDir = cacheBaseDir
        }
        return cacheDir
    }


    /**
     * 得到缓存目录

     * @param context
     * *
     * @return
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun getCacheBaseDir(context: Context): File? {

        var baseDir: File? = null
        if (Build.VERSION.SDK_INT >= 8) {
            baseDir = context.externalCacheDir
        } else {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
                baseDir = Environment.getExternalStorageDirectory()
        }

        if (baseDir == null)
            baseDir = context.cacheDir

        if (baseDir != null) {
            val cacheBaseDir = File(baseDir, cacheBaseDirName)
            if (!cacheBaseDir.exists())
                cacheBaseDir.mkdirs()
            return cacheBaseDir
        }
        return null
    }


    /**
     * 获取缓存路径

     * @param context
     * *
     * @return 返回缓存文件路径
     */
    fun getCacheImageDir(context: Context): File {
        val imageCache: File
        if (hasExternalStorage()) {
            imageCache = File(context.externalCacheDir, CACHE_DIR)
        } else {
            imageCache = File(context.cacheDir, CACHE_DIR)
        }
        if (!imageCache.exists())
            imageCache.mkdirs()
        return imageCache
    }

    private fun hasExternalStorage(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * 保存图片到本地

     * @param bitmap
     */
    fun saveBitmap(bitmap: Bitmap, filename: String): File? {

        val parent = File(DefImageLoader.default.mDefaultImageReceivedPath)
        if (!parent.exists()) {
            parent.mkdirs()
        }

        val file = File(DefImageLoader.default.mDefaultImageReceivedPath, filename)
        if (file.exists()) {
            file.delete()
        }
        try {

            file.createNewFile()
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            return file
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            file.delete()
        } catch (e: IOException) {
            e.printStackTrace()
            file.delete()
        }

        return null
    }

    fun getOrientation(path: String): Int {
        var orientation = 0
        if (TextUtils.isEmpty(path))
            return orientation

        val file = File(path)
        if (!file.exists())
            return orientation

        try {
            val exifInterface = ExifInterface(path)
            when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> orientation = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> orientation = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> orientation = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return orientation

    }

    /**
     * byte[]转Bitmap
     */
    fun Bytes2Bitmap(b: ByteArray): Bitmap? {
        if (b.size != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.size)
        } else {
            return null
        }
    }


    /**
     * 这个很有意思的是， MediaStore.Images.Media.insertImage 会把文件复制一份，
     * 结果会在系统图库里面有两张一样的图片

     * @param context
     * *
     * @param file
     */
    fun saveImageToGallery(context: Context, file: File) {

        // 其次把文件插入到系统图库
        /* try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        // 最后通知图库更新
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + file.absolutePath)))
    }


    /**
     * 获取文件夹大小

     * @param file File实例
     * *
     * @return long
     */
    fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            for (loop in fileList) {
                if (loop.isDirectory)
                    size += getFolderSize(loop)
                else
                    size += loop.length()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return size
    }

    /**
     * 删除文件夹，及文件夹下面的文件

     * @param file
     * *
     * @return long
     */
    fun removeDirectory(file: File): Long {
        val size: Long = 0
        try {
            val fileList = file.listFiles()
            for (i in fileList.indices) {
                if (fileList[i].isDirectory) {
                    removeDirectory(fileList[i])
                } else {
                    fileList[i].delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return size
    }

    /**
     * 删除指定目录下文件及目录

     * @param deleteThisPath
     * *
     * @param filePath
     * *
     * @return
     */
    fun deleteFolderFile(filePath: String, deleteThisPath: Boolean) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                val file = File(filePath)
                if (file.isDirectory) {// 处理目录
                    val files = file.listFiles()
                    for (i in files.indices) {
                        deleteFolderFile(files[i].absolutePath, true)
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory) {// 如果是文件，删除
                        file.delete()
                    } else {// 目录
                        if (file.listFiles().isEmpty()) {// 目录下没有文件或者目录，删除
                            file.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 格式化单位

     * @param size
     * *
     * @return
     */
    fun getFormatSize(size: Double): String {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
            return size.toString() + "B"
        }

        val megaByte = kiloByte / 1024
        //        if(megaByte < 1) {
        //            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
        //            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "K";
        //        }

        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(java.lang.Double.toString(megaByte))
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "M"
        }

        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(java.lang.Double.toString(gigaByte))
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "G"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "T"
    }


    fun createTmpFile(context: Context): File {

        val state = Environment.getExternalStorageState()
        if (state == Environment.MEDIA_MOUNTED) {
            // 已挂载
            val pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!pic.exists())
                pic.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
            val fileName = timeStamp + ".jpg"
            val tmpFile = File(pic, fileName)
            return tmpFile
        } else {
            val cacheDir = context.cacheDir
            if (!cacheDir.exists())
                cacheDir.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
            val fileName = timeStamp + ".jpg"
            val tmpFile = File(cacheDir, fileName)
            return tmpFile
        }

    }


    fun copyFile(src: File, path: String, name: String): File? {
        var dest: File?
        if (!src.exists()) {
            Log.e(TAG, "copyFile: src file does not exist! -" + src.absolutePath)
            return null
        } else {
            dest = File(path)
            if (!dest.exists()) {
                Log.d(TAG, "copyFile: dir does not exist!")
                dest.mkdirs()
            }

            dest = File(path + name)

            try {
                val e = FileInputStream(src)
                val fos = FileOutputStream(dest)
                val buffer = ByteArray(1024)

                var length: Int = e.read(buffer)
                while (length != -1) {
                    fos.write(buffer, 0, length)
                    length = e.read(buffer)
                }

                fos.flush()
                fos.close()
                e.close()
                return dest
            } catch (var8: IOException) {
                var8.printStackTrace()
                Log.e(TAG, "copyFile: Exception!")
                return dest
            }

        }
    }

}
