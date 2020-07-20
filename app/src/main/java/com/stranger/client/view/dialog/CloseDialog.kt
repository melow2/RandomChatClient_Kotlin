package com.stranger.client.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.hyeoksin.admanager.AdManager
import com.hyeoksin.admanager.data.Ad
import com.hyeoksin.admanager.data.AdName
import com.hyeoksin.admanager.data.AdType
import com.stranger.client.R
import com.stranger.client.databinding.DialogCloseBinding
import java.util.*

class CloseDialog(
    private val mContext: Context
):Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar) {

    var listener: ButtonEvent? = null

    companion object {
        val TAG = CloseDialog::class.java.simpleName
    }

    interface ButtonEvent {
        fun onPositiveBtn()
        fun onNegativeBtn()
    }

    fun addButtonListener(listener: ButtonEvent?) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding: DialogCloseBinding = DialogCloseBinding.inflate(LayoutInflater.from(context))
        setContentView(mBinding.root)

        mBinding.tvDialogCommonTitle.text = String.format(Locale.getDefault(), context.getString(R.string.msg_exit_close), context.getString(R.string.app_name))

        val adManager = AdManager.Builder(mContext)
                .setAdmangerTest(true)
                .setContainer(mBinding.dialogCommonContent)
                .setAd(Ad(AdName.ADMOB, AdType.HALF_BANNER, mContext.getString(R.string.admob_banner_popup)))
                .setAd(Ad(AdName.FACEBOOK, AdType.HALF_BANNER, mContext.getString(R.string.facebook_banner_popup)))
                .build();
        adManager.load();
        mBinding.btnFinish.setOnClickListener { listener!!.onPositiveBtn() }
        mBinding.btnCancel.setOnClickListener { listener!!.onNegativeBtn() }
    }

}