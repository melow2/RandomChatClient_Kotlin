package com.stranger.client.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.experimental.or

/*
* 양방향 암호화 알고리즘인 AES256 암호화를 지원하는 서비스.
* */
object DataSecurityUtil {
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