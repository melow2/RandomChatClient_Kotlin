package com.hellostranger.client

import android.content.Context
import android.view.View
import com.hellostranger.client.databinding.MainActivityBinding

class MainHandler(private val mContext: Context, binding: MainActivityBinding) {
    private val mMainActivityBinding: MainActivityBinding = binding
    private var mListener: MainHandlerEvent? = null

    interface MainHandlerEvent {
        fun onClickSendBtn(msg: String?)
        fun onClickBtnReload(msg: String?)
    }

    fun addEventListener(listener: MainHandlerEvent?) {
        mListener = listener
    }

    fun onClickSendBtn(view: View?) {
        mListener!!.onClickSendBtn(mMainActivityBinding.edtMsg.getText().toString())
    }

    fun onClickBtnReload(view: View?) {
        mListener!!.onClickBtnReload(mContext.getString(R.string.msg_reload))
    }

}