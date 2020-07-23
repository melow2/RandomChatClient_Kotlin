package com.stranger.client.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.stranger.client.R
import com.stranger.client.util.SecureSharedPreferences
import java.util.*


class RateItDialogFragment : DialogFragment() {
    companion object {
        private const val LAUNCHES_UNTIL_PROMPT = 5     // 5번 실행 제한.
        private const val DAYS_UNTIL_PROMPT = 2         // 2일
        private const val MILLIS_UNTIL_PROMPT = DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000 // 하루
        private const val PREF_NAME = "APP_RATER"       // SharedPreference Name
        private const val LAST_PROMPT = "LAST_PROMPT"   // 마지막 시간
        private const val LAUNCHES = "LAUNCHES"         // 실행 횟수.
        private const val DISABLED = "DISABLED"         // 활성화 여부

        fun show(context: Context, fragmentManager: FragmentManager) {
            var shouldShow = false
            val sharedPreferences = SecureSharedPreferences.wrap(getSharedPreferences(context), context);
            val currentTime = System.currentTimeMillis()                    // 현재 시간.
            var lastPromptTime = sharedPreferences.get(LAST_PROMPT, 0L)  // 마지막 실행 시간.
            if (lastPromptTime == 0L) {
                lastPromptTime = currentTime
                sharedPreferences.put(LAST_PROMPT, lastPromptTime)
            }
            if (!sharedPreferences.get(DISABLED, false)) {               // 아직 5번 이하로 실행했다면.
                val launches = sharedPreferences.get(LAUNCHES, 0) + 1        // 실행 횟수 1회 추가.
                if (launches >= LAUNCHES_UNTIL_PROMPT) {                        // 실행 횟수가 5회 이상이라면.
                    if (currentTime >= lastPromptTime + MILLIS_UNTIL_PROMPT) {   // 현재 시간이 마지막 실행시간에서 2일이 지나있다면.
                        shouldShow = true
                    }
                }
                sharedPreferences.put(LAUNCHES, launches)   // 실행횟수 추가.
            }
            if (shouldShow) {
                sharedPreferences.put(LAUNCHES, 0)
                sharedPreferences.put(LAST_PROMPT, System.currentTimeMillis())
                RateItDialogFragment().show(fragmentManager, "REVIEW")
            }
        }

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, 0)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sharedPreferences = SecureSharedPreferences.wrap(getSharedPreferences(activity!!), activity!!)
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
                sharedPreferences.put(DISABLED, true)
                dismiss()
            }
            .setNeutralButton(R.string.msg_rate_remind_later) { dialog, which -> dismiss() }
            .setNegativeButton(R.string.msg_rate_never) { dialog, which ->
                sharedPreferences.put(DISABLED, true)
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