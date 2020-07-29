package com.happiness.lovely.view.activity

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import com.happiness.lovely.R

abstract class BaseActivity<Z : ViewDataBinding> : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    lateinit var mBinding:Z
    protected fun bindView(layout: Int) {
        mBinding = DataBindingUtil.setContentView<Z>(this, layout)
    }

    protected fun setToolbar(
        toolbar: Toolbar,
        backBtnVisible: Boolean,
        toolbarTitle: String,
        tvToolbarTitle: TextView
    ) {
        this.toolbar = toolbar
        toolbar.title = "" // 기존의 툴바 타이틀 제거.
        toolbar.setContentInsetsAbsolute(0, 0) // 좌우 여백 제거.
        tvToolbarTitle.text = toolbarTitle
        // tvToolbarTitle.setTextColor(resources.getColor(R.color.colorRed))
        if (backBtnVisible) { // 뒤로가기 버튼 보이기.
            // toolbar.setNavigationIcon(R.drawable.ic_share_accent)
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }
        setSupportActionBar(toolbar)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // 슬라이드 애니메이션.
        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }

    companion object {
        private val TAG = BaseActivity::class.java.simpleName
        protected fun startActivityAnimation(context: Context) {
            if (context is AppCompatActivity) {
                context.overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }
        }

        fun printLog(tag: String, msg: String) {
            Log.d(tag, msg)
        }

        fun showSnackBar(v: View, @StringRes stringResID: Int) {
            Snackbar.make(v, stringResID, Snackbar.LENGTH_LONG).show()
        }

        fun showToast(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}