package com.stranger.client.util

import android.content.Context
import android.content.SharedPreferences


class SecureSharedPreferences(
    private val mSharedPref: SharedPreferences,
    private val context: Context) {

    fun contains(key: String) = mSharedPref.contains(key)

    fun get(key: String, defaultValue: Boolean): Boolean = getInternal(key, defaultValue)
    fun get(key: String, defaultValue: Int): Int = getInternal(key, defaultValue)
    fun get(key: String, defaultValue: Long): Long = getInternal(key, defaultValue)
    fun get(key: String, defaultValue: String): String = getInternal(key, defaultValue)

    private fun <T : Any> getInternal(key: String, defaultValue: T): T {
        var data: String? =mSharedPref.getString(key,"")
        if(data.isNullOrEmpty())
            return defaultValue
        val value = DataSecurityUtil.decryptText(context,data)
        @Suppress("PlatformExtensionReceiverOfInline", "UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        return when(defaultValue) {
            is Boolean -> value?.toBoolean()
            is Int     -> value?.toInt()
            is Long    -> value?.toLong()
            is String  -> value
            else -> throw IllegalArgumentException("defaultValue only could be one of these types: Boolean, Int, Long, String")
        } as T
    }

    fun put(key: String, value: Boolean) = putInternal(key, value)
    fun put(key: String, value: Int) = putInternal(key, value)
    fun put(key: String, value: Long) = putInternal(key, value)
    fun put(key: String, value: String) = putInternal(key, value)

    private fun putInternal(key: String, value: Any?) {
        try {
            mSharedPref.edit().run {
                if (value == null) {
                    remove(key)
                } else {
                    putString(key, DataSecurityUtil.encryptText(context,value.toString()))
                }
                apply()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    companion object {
        fun wrap(sharedPref: SharedPreferences,context: Context) = SecureSharedPreferences(sharedPref,context)
    }
}