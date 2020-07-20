package com.stranger.client.view.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.hyeoksin.admanager.AdManager
import com.hyeoksin.admanager.OnInterstitialAdLoadListener
import com.hyeoksin.admanager.data.Ad
import com.hyeoksin.admanager.data.AdName
import com.hyeoksin.admanager.data.AdType
import com.stranger.client.R
import com.stranger.client.core.ClientAsyncTask
import com.stranger.client.core.RandomChatLog
import com.stranger.client.core.SocketManager.exit
import com.stranger.client.databinding.MainActivityBinding
import com.stranger.client.view.dialog.RateItDialogFragment
import com.stranger.client.view.dialog.CloseDialog
import com.stranger.client.view.dialog.NoticeDialogFragment
import com.stranger.client.view.dialog.SelectSexDialog
import com.stranger.client.view.handler.MainHandler
import com.stranger.client.view.handler.WeakHandler
import java.util.*


class MainActivity : BaseActivity<MainActivityBinding>() {

    private lateinit var mWeakHandler: WeakHandler
    private var mLastClickTime:Long = 0

    companion object {
        var interstitialAd: AdManager? = null
        var bottomAd: AdManager? = null
        lateinit var closeDialog:CloseDialog
        private lateinit var CURRENT_SEX: String
        private const val MALE = "M"
        private const val FEMALE = "F"
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindView(R.layout.activity_main);
        setToolbar(
            mBinding.toolbar as Toolbar,
            false,
            getString(applicationInfo.labelRes),
            findViewById(R.id.tv_title)
        )
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.menu)
        setNavigation()
        setAdvertisement()
        setPopupSex()
        init()
    }


    private fun init() {
        RateItDialogFragment.show(MainActivity@this,supportFragmentManager)
        mWeakHandler = WeakHandler(Looper.getMainLooper())
        val eventHandler = MainHandler(this, mBinding)
        mBinding.edtMsg.filters = arrayOf<InputFilter>(LengthFilter(500))
        eventHandler.addEventListener(object :
            MainHandler.MainHandlerEvent {

            @Suppress("DEPRECATION")
            override fun onClickSendBtn(msg: String?) {
                val sendMessage = msg!!.trim { it <= ' ' }
                println(sendMessage.length)
                if (sendMessage.isNotEmpty()) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 200) {
                        showToast(this@MainActivity, "천천히")
                        return
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()
                    ClientAsyncTask.SendMessageTask().execute(sendMessage)
                    addView(msg, 3)
                }
                mBinding.edtMsg.setText("")
            }

            override fun onClickBtnReload(msg: String?) {
                reConnect(msg)
            }
        })
        mBinding.handler = eventHandler
    }

    @Suppress("DEPRECATION")
    private fun reConnect(msg: String?) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle(null)
        builder.setMessage(msg)
        builder.setPositiveButton("확인") { dialog: DialogInterface?, which: Int ->
            mBinding.lytMsgline.removeAllViews()
            ClientAsyncTask.ReConnectTask().execute()
        }
        builder.setCancelable(true)
        builder.create()
        builder.show()
    }

    private fun setNavigation() {
        mBinding.navigationView.setNavigationItemSelectedListener { menuItem ->
            // menuItem.isChecked = true
            when (menuItem.itemId) {
                R.id.navigation_item_notice -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    val prev = supportFragmentManager.findFragmentByTag("NOTICE")
                    if(prev!=null) {
                        // back 버튼을 눌렀을 경우 사라지게 됨.
                        fragmentTransaction.remove(prev)
                    }
                    fragmentTransaction.addToBackStack(null)
                    val newFragment = NoticeDialogFragment.newInstance(3)
                    newFragment.show(fragmentTransaction,"NOTICE")
                }
            }
            mBinding.drawerLayout.closeDrawers()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> mBinding.drawerLayout.openDrawer(GravityCompat.START)
            R.id.share -> {
                val title = String.format(Locale.getDefault(),
                    getString(R.string.msg_share_message),
                    getString(applicationInfo.labelRes)
                ) // app_name
                val content = String.format(
                    Locale.getDefault(),
                    getString(R.string.share_store_url),
                    packageName
                ) // com.flower
                val intent = Intent(Intent.ACTION_SEND) // 공유하기.
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_SUBJECT, getString(applicationInfo.labelRes))
                    .putExtra(Intent.EXTRA_TEXT, """$title $content""".trimIndent())
                startActivity(Intent.createChooser(intent, getString(R.string.share_app)))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAdvertisement() {
        setPopupAd()
        interstitialAd = AdManager.Builder(this@MainActivity)
            .setAdmangerTest(true)
            .setAd(Ad(AdName.ADMOB, AdType.INTERSTITIAL, getString(R.string.admob_interstitial)))
            .setAd(Ad(AdName.FACEBOOK, AdType.INTERSTITIAL, getString(R.string.facebook_interstitial)))
            .setOnInterstitialAdLoadListener(object : OnInterstitialAdLoadListener {
                override fun onAdLoaded() {
                    interstitialAd?.showInterstitial()
                }

                override fun onAdFailedToLoad() {}
            })
            .build()
        bottomAd = AdManager.Builder(this@MainActivity)
            .setContainer(mBinding.adContainer)
            .setAd(Ad(AdName.ADMOB, AdType.BANNER, getString(R.string.admob_banner_bottom)))
            .setAd(Ad(AdName.FACEBOOK, AdType.BANNER, getString(R.string.facebook_banner_bottom)))
            .build()
        interstitialAd?.load()
        bottomAd?.load()

    }

    private fun setPopupAd() {
        closeDialog = CloseDialog(mContext = this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            closeDialog.create();
        }
        closeDialog.addButtonListener(object : CloseDialog.ButtonEvent {
            override fun onPositiveBtn() {
                closeDialog.dismiss()
                finish()
            }

            override fun onNegativeBtn() {
                closeDialog.dismiss()
            }
        })
    }

    private fun setPopupSex() {
        val selectSexDialog = SelectSexDialog(mContext = this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            selectSexDialog.create();
        }
        selectSexDialog.addButtonListener(object :
            SelectSexDialog.Event {
            @Suppress("DEPRECATION")
            override fun onClickMale() {
                CURRENT_SEX =
                    MALE
                mBinding.scvMsgItem.setBackgroundColor(resources.getColor(R.color.colorChangedScrollViewBackground))
                ClientAsyncTask.ServerConnectTask(this@MainActivity, mBinding, CURRENT_SEX)
                    .execute()
                selectSexDialog.dismiss()
            }

            @Suppress("DEPRECATION")
            override fun onClickFemale() {
                CURRENT_SEX =
                    FEMALE
                ClientAsyncTask.ServerConnectTask(
                    this@MainActivity,
                    mBinding,
                    CURRENT_SEX
                )
                    .execute()
                selectSexDialog.dismiss()
            }
        })
        selectSexDialog.setCancelable(false)
        selectSexDialog.show()
    }

    override fun onBackPressed() {
        closeDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        exit()
    }

    private fun addView(msg: String, i: Int) {
        mWeakHandler.post {
            mBinding.lytMsgline.addView(
                RandomChatLog(
                    this@MainActivity,
                    mBinding,
                    msg,
                    null,
                    i
                )
            )
            mBinding.scvMsgItem.post { mBinding.scvMsgItem.fullScroll(View.FOCUS_DOWN) }
            mBinding.edtMsg.requestFocus()
        }
    }

}