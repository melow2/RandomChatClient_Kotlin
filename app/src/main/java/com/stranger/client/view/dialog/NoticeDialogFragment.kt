package com.stranger.client.view.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.stranger.client.R
import com.stranger.client.databinding.DialogNoticeBinding

class NoticeDialogFragment : DialogFragment() {

    private var mStyle: Int = 0

    companion object {
        fun newInstance(style: Int): NoticeDialogFragment {
            val fragment = NoticeDialogFragment()
            val args = Bundle()
            args.putInt("style", style)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mBinding:DialogNoticeBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_notice,container,false)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)                 // 타이틀 없애기.
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 모서리 둥글게.
        return mBinding.root
    }
}