package com.stranger.client.util

import android.content.Context
import android.content.SharedPreferences

class StarPointCounter {
    companion object {
        private const val DAYS_UNTIL_PROMPT = 0         // 2일
        private const val MILLIS_UNTIL_PROMPT = DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000 // 하루
        const val PREF_NAME = "STAR_COUNTER"       // SharedPreference Name
        private const val LAST_PROMPT = "LAST_PROMPT"   // 마지막 시간
        const val STAR_COUNT = "LAUNCHES"         // 실행 횟수.
        fun count(context: Context): Int {
            // 하루에 앱 실행 1번 시 포인트 2포인트 충전.
            var starCount= 0
            val sharedPreferences = SecureSharedPreferences.wrap(getSharedPreferences(context), context);
            val currentTime = System.currentTimeMillis()                            // 현재 시간.
            var lastPromptTime = sharedPreferences.get(LAST_PROMPT, 0L)  // 마지막 실행 시간.

            if (lastPromptTime == 0L) {
                lastPromptTime = currentTime
                sharedPreferences.put(LAST_PROMPT, lastPromptTime)
            }
            if (currentTime >= lastPromptTime + MILLIS_UNTIL_PROMPT) {
                // 현재 시간이 마지막 실행 시간에서 하루가 지나있다면.
                starCount = sharedPreferences.get(STAR_COUNT,0)+10
                sharedPreferences.put(STAR_COUNT,starCount)
                sharedPreferences.put(LAST_PROMPT, currentTime)
            }
            return starCount;
        }

        fun decreaseCount(context: Context): Int {
            val sharedPreferences = SecureSharedPreferences.wrap(getSharedPreferences(context), context);
            val count = sharedPreferences.get(STAR_COUNT,0)-10
            sharedPreferences.put(STAR_COUNT,count)
            return count
        }

        fun getStartCount(context: Context):Int{
            val sharedPreferences = SecureSharedPreferences.wrap(getSharedPreferences(context), context);
            return sharedPreferences.get(STAR_COUNT,0)
        }

        fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, 0)
        }
    }

}