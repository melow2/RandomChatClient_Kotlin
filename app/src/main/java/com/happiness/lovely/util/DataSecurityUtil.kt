package com.happiness.lovely.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import com.happiness.lovely.core.MessageConstants.SKSP
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object DataSecurityUtil {
    private val ivBytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

    // 32바이트
    fun defaultEncryption(str: String): String {
        try {
            val textBytes = str.toByteArray(charset("UTF-8"))
            val ivSpec: AlgorithmParameterSpec = IvParameterSpec(ivBytes)
            val newKey = SecretKeySpec(SKSP.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec)
            return Base64.encodeToString(cipher.doFinal(textBytes), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun defaultDecryption(str: String): String {
        try {
            val textBytes = Base64.decode(str, 0)
            val ivSpec: AlgorithmParameterSpec = IvParameterSpec(ivBytes)
            val newKey = SecretKeySpec(SKSP.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec)
            return String(cipher.doFinal(textBytes), charset("UTF-8"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun encryptText(context: Context, text: String): String? {
        try {
            var encryptedByte = text.toByteArray()
            val key = getSecretKey(context)
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            encryptedByte = cipher.doFinal(encryptedByte)
            return Base64.encodeToString(encryptedByte, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    @SuppressLint("PackageManagerGetSignatures")
    @Throws(PackageManager.NameNotFoundException::class, NoSuchAlgorithmException::class)
    private fun getSecretKey(context: Context): SecretKey? {
        var key: SecretKey? = null
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        for (signature in packageInfo.signatures) {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(signature.toByteArray())
            key = SecretKeySpec(md.digest(), "AES")
            break
        }
        return key
    }

    fun decryptText(context: Context, encryptedText: String?): String? {
        try {
            val encryptedByte = Base64.decode(encryptedText, Base64.DEFAULT)
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(context)) // secret key 는 위에서 생성하는 signkey 로부터 생성한 녀석과 같다.
            val decryptedByte = cipher.doFinal(encryptedByte)
            return String(decryptedByte, charset("UTF-8"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getDeviceId(mContext:Context):String{
        return "DEVICE_SAMPLE"
    }
}