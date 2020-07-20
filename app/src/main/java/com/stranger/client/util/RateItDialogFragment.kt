package com.stranger.client.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.stranger.client.R
import java.util.*


class RateItDialogFragment : DialogFragment() {
    companion object {
        private const val LAUNCHES_UNTIL_PROMPT = 5    // 5번 실행 제한.
        private const val DAYS_UNTIL_PROMPT = 2         // 2일
        private const val MILLIS_UNTIL_PROMPT = DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000 // 하루
        private const val PREF_NAME = "APP_RATER"       // SharedPreference Name
        private const val LAST_PROMPT = "LAST_PROMPT"   // 마지막 시간
        private const val LAUNCHES = "LAUNCHES"         // 실행 횟수.
        private const val DISABLED = "DISABLED"         // 활성화 여부

        fun show(context: Context?, fragmentManager: FragmentManager) {
            var shouldShow = false
            val sharedPreferences = getSharedPreferences(context!!)
            val editor = sharedPreferences!!.edit()
            val currentTime = System.currentTimeMillis()                    // 현재 시간.
            var lastPromptTime = sharedPreferences.getLong(LAST_PROMPT, 0)  // 마지막 실행 시간.
            if (lastPromptTime == 0L) {
                lastPromptTime = currentTime
                editor.putLong(LAST_PROMPT, lastPromptTime)
            }
            if (!sharedPreferences.getBoolean(DISABLED, false)) {               // 아직 5번 이하로 실행했다면.
                val launches = sharedPreferences.getInt(LAUNCHES, 0) + 1        // 실행 횟수 1회 추가.
                if (launches >= LAUNCHES_UNTIL_PROMPT) {                        // 실행 횟수가 5회 이상이라면.
                    if (currentTime >= lastPromptTime + MILLIS_UNTIL_PROMPT) {   // 현재 시간이 마지막 실행시간에서 2일이 지나있다면.
                        shouldShow = true
                    }
                }
                editor.putInt(LAUNCHES, launches)   // 실행횟수 추가.
            }
            if (shouldShow) {
                editor.putInt(LAUNCHES, 0).putLong(LAST_PROMPT, System.currentTimeMillis()).apply()
                RateItDialogFragment().show(fragmentManager, null)
            } else {
                editor.commit()
            }
        }

        private fun getSharedPreferences(context: Context): SharedPreferences? {
            return context.getSharedPreferences(PREF_NAME, 0)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
            .setTitle(R.string.msg_rate_title)
            .setMessage(R.string.msg_rate_message)
            .setPositiveButton(R.string.msg_rate_positive) { dialog, which ->
                val addr = String.format(
                    Locale.getDefault(),
                    getString(R.string.share_store_url),
                    activity!!.packageName
                )
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(addr)))
                getSharedPreferences(activity!!)!!.edit().putBoolean(DISABLED, true).apply()
                dismiss()
            }
            .setNeutralButton(R.string.msg_rate_remind_later) { dialog, which -> dismiss() }
            .setNegativeButton(R.string.msg_rate_never) { dialog, which ->
                getSharedPreferences(activity!!)!!.edit().putBoolean(DISABLED, true).apply()
                dismiss()
            }
        val dialog = builder.create();
        dialog.setOnShowListener {
            val negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            val neutralBtn = dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
            val positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            neutralBtn.setTextColor(resources.getColor(R.color.colorTextTime))
            negativeBtn.setTextColor(resources.getColor(R.color.colorRed))
            positiveBtn.setTextColor(resources.getColor(R.color.colorPrimaryDark))
        }
        return dialog
    }
}