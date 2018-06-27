package org.niantuo.rxretrofit.utils


import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * 提供MD5加密方法
 *
 *
 * Description:描述
 *
 */
object MD5 {

    private val TAG = "MD5"

    /**
     * 对输入文本进行MD5加密
     * @param info 被加密文本，如果输入为null则返回null
     * *
     * @return MD5 加密后的内容，如果为null说明输入为null
     */
    fun getMD5(info: String): String {
        return getMD5(info.toByteArray())
    }

    /**
     * 对输入文本进行MD5加密
     * @param info 被加密文本 如果输入为null则返回null
     * *
     * @return MD5加密后的内容，如果为null说明输入为null
     */
    fun getMD5(info: ByteArray?): String {


        if (null == info || info.isEmpty()) {
            return ""
        }
        val buf = StringBuffer("")
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return ""
        }

        md.update(info)
        val b = md.digest()
        var i: Int

        for (offset in b.indices) {
            i = b[offset].toInt()
            if (i < 0)
                i += 256
            if (i < 16)
                buf.append("0")
            buf.append(Integer.toHexString(i))
        }
        return buf.toString()
    }

}
