package com.happiness.lovely.view.dialog

import android.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.happiness.lovely.databinding.DialogSelectSexBinding
import java.io.IOException

class SelectSexDialog(val mContext: Context)
    : Dialog(mContext, R.style.Theme_Translucent_NoTitleBar) {
    var listener: Event? = null


    companion object {
        val TAG = SelectSexDialog::class.java.simpleName
    }

    interface Event {
        @Throws(IOException::class)
        fun onClickMale()

        @Throws(IOException::class)
        fun onClickFemale()
    }

    fun addButtonListener(listener: Event?) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding: DialogSelectSexBinding = DialogSelectSexBinding.inflate(LayoutInflater.from(mContext))
        setContentView(mBinding.root)
        mBinding.ivFemale.setOnClickListener { listener!!.onClickFemale() }
        mBinding.ivMale.setOnClickListener { listener!!.onClickMale() }
    }

}