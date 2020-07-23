package com.stranger.client.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.stranger.client.R
import com.stranger.client.core.MessageConstants.FEMALE
import com.stranger.client.core.MessageConstants.MALE
import com.stranger.client.core.MessageConstants.RANDOM
import com.stranger.client.databinding.DialogReconnectBinding
import com.stranger.client.util.StarPointCounter

class ReconnectDialog(
    private val mContext: Context
):Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar) {

    var listener: ButtonEvent? = null
    lateinit var mBinding:DialogReconnectBinding

    interface ButtonEvent {
        fun onReconnectBtn(selected:String)
    }

    fun addButtonListener(listener: ButtonEvent?) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DialogReconnectBinding.inflate(LayoutInflater.from(context))
        var selected:String = RANDOM
        mBinding.rgPrefer.setOnCheckedChangeListener { radioGroup, id ->
            when(id){
                R.id.rbtn_male->{ selected= MALE }
                R.id.rbtn_female->{ selected=FEMALE }
                R.id.rbtn_random->{ selected=RANDOM }
            }
        }
        setContentView(mBinding.root)
        mBinding.btnReconnect.setOnClickListener { listener!!.onReconnectBtn(selected) }
    }

    override fun show() {
        super.show()
        mBinding.tvStarCount.text = "x"+StarPointCounter.getStartCount(mContext).toString()
    }

}