package com.hellostranger.client

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.hellostranger.client.databinding.ChatlogCommandBinding
import com.hellostranger.client.databinding.ChatlogLeftBinding
import com.hellostranger.client.databinding.ChatlogRightBinding
import com.hellostranger.client.databinding.MainActivityBinding
import java.text.SimpleDateFormat
import java.util.*

class RandomChatLog(
    private val mContext: Context,
    private val mBinding: MainActivityBinding,
    private val mReceivedMsg: String,
    private val mClientInfo: String?,
    private val mCommand: Int
) : LinearLayout(mContext) {

    init {
        writeLog()
    }

    companion object {
        private val TAG = RandomChatLog::class.java.simpleName
        private var CURRENT_LOG = "ME"
        private const val STATE_STRANGER = "STRANGER"
        private const val STATE_ME = "ME"
        private const val MAIL = "M"
        private const val FEMAIL = "F"
    }

    private fun writeLog() {
        when (mCommand) {
            1 -> {
                val mBindingLeft: ChatlogLeftBinding = DataBindingUtil.inflate(
                    (mContext as MainActivity).layoutInflater,
                    R.layout.chatlog_left,
                    this,
                    true
                )
                if (CURRENT_LOG != STATE_STRANGER) {
                    mBindingLeft.ivProfile.visibility = View.VISIBLE
                    mBindingLeft.tvName.visibility = View.VISIBLE
                    if (mClientInfo == FEMAIL) {
                        mBindingLeft.tvName.text = mContext.getString(R.string.female)
                        mBindingLeft.tvMsg.setBackgroundResource(R.drawable.background_chatlog_left_female)
                        mBindingLeft.ivProfile.setImageResource(R.drawable.icons_female_profile_512)
                    } else {
                        mBindingLeft.tvName.text = mContext.getString(R.string.male)
                    }
                    CURRENT_LOG = STATE_STRANGER
                } else {
                    if (mClientInfo == FEMAIL) {
                        mBindingLeft.tvMsg.setBackgroundResource(R.drawable.background_chatlog_left_female)
                    }
                    mBindingLeft.ivProfile.visibility = View.INVISIBLE
                    mBindingLeft.tvName.visibility = View.GONE
                }
                mBindingLeft.tvMsg.text = mReceivedMsg
                mBindingLeft.tvTime.text = currentTime
            }
            2 -> {
                val mBindingCommand: ChatlogCommandBinding = DataBindingUtil.inflate(
                    (mContext as MainActivity).layoutInflater,
                    R.layout.chatlog_command,
                    this,
                    true
                )
                mBindingCommand.tvCommand.text = mReceivedMsg
                CURRENT_LOG = STATE_ME
            }
            3 -> {
                val mBindingRight: ChatlogRightBinding = DataBindingUtil.inflate(
                    (mContext as MainActivity).layoutInflater,
                    R.layout.chatlog_right,
                    this,
                    true
                )
                mBindingRight.tvMsg.text = mReceivedMsg
                mBindingRight.tvTime.text = currentTime
                CURRENT_LOG = STATE_ME
            }
        }
    }

    private val currentTime: String
        get() {
            var hour = SimpleDateFormat("kk").format(Date()).toInt()
            val minute = SimpleDateFormat("mm").format(Date()).toInt()
            var curTime = ""
            if (12 < hour) {
                hour -= 12
                curTime += "오후 $hour"
            } else {
                curTime += "오전 $hour"
            }
            curTime += if (minute < 10) ":0$minute" else ":$minute"
            return curTime
        }
}