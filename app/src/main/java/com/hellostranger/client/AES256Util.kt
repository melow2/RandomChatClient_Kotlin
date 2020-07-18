package com.hellostranger.client

import android.util.Base64
import com.hellostranger.client.MessageConstants.MSG_BUSY
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/*
* 양방향 암호화 알고리즘인 AES256 암호화를 지원하는 서비스.
* */
object AES256Util {
    private val ivBytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    // 키 사이즈가 32바이트.
    fun encryption(str: String): String? {
        try {
            val textBytes = str.toByteArray(charset("UTF-8"))
            val ivSpec: AlgorithmParameterSpec = IvParameterSpec(ivBytes)
            val newKey = SecretKeySpec(MSG_BUSY.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec)
            return Base64.encodeToString(cipher.doFinal(textBytes), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decryption(str: String?): String? {
        try {
            val textBytes = Base64.decode(str, 0)
            val ivSpec: AlgorithmParameterSpec = IvParameterSpec(ivBytes)
            val newKey = SecretKeySpec(MSG_BUSY.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec)
            return String(cipher.doFinal(textBytes), charset("UTF-8"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}