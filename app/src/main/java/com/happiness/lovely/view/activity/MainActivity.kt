package com.happiness.lovely.view.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.hyeoksin.admanager.AdManager
import com.hyeoksin.admanager.OnInterstitialAdLoadListener
import com.hyeoksin.admanager.data.Ad
import com.hyeoksin.admanager.data.AdName
import com.hyeoksin.admanager.data.AdType
import com.happiness.lovely.R
import com.happiness.lovely.core.ClientAsyncTask
import com.happiness.lovely.core.MessageConstants.FEMALE
import com.happiness.lovely.core.MessageConstants.MALE
import com.happiness.lovely.core.RandomChatLog
import com.happiness.lovely.core.SocketManager.exit
import com.happiness.lovely.databinding.MainActivityBinding
import com.happiness.lovely.util.StarPointCounter
import com.happiness.lovely.view.dialog.*
import com.happiness.lovely.view.handler.MainHandler
import com.happiness.lovely.view.handler.WeakHandler
import model.SocketClient
import timber.log.Timber
import java.util.*


class MainActivity : BaseActivity<MainActivityBinding>() {

    private lateinit var mWeakHandler: WeakHandler
    private var mLastClickTime: Long = 0

    companion object {
        lateinit var CURRENT_SEX:String
        var interstitialAd: AdManager? = null
        var bottomAd: AdManager? = null
        lateinit var closeDialog: CloseDialog
        lateinit var reconnectDialog: ReconnectDialog
        fun addView(activity:MainActivity,msg: String, clientInfo: SocketClient?, i: Int) {
            val mWeakHandler = WeakHandler(Looper.getMainLooper())
            mWeakHandler.post {
                activity.mBinding.lytMsgline.addView(RandomChatLog(activity, msg, clientInfo, i))
                activity.mBinding.scvMsgItem.post { activity.mBinding.scvMsgItem.fullScroll(View.FOCUS_DOWN) }
                activity.mBinding.edtMsg.requestFocus()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        bindView(R.layout.activity_main);
        setToolbar(
            mBinding.toolbar as Toolbar,
            false,
            getString(applicationInfo.labelRes),
            findViewById(R.id.tv_title)
        )
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.icon_menu)
        init()
        setNavigation()
        setAdvertisement()
        setPopupSexDialog()
        setReconnectDialog()
    }

    private fun setReconnectDialog() {
        val headerStartCount = mBinding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.tv_star_count)
        reconnectDialog = ReconnectDialog(mContext = this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reconnectDialog.create();
        }
        reconnectDialog.addButtonListener(object : ReconnectDialog.ButtonEvent {
            override fun onReconnectBtn(selected: String) {
                if(selected == MALE || selected == FEMALE) {
                    if (StarPointCounter.getStartCount(this@MainActivity) <= 0) {
                        showToast(this@MainActivity, "원하는 성별 선택 시 별10개가 필요합니다.")
                        return
                    } else {
                        mBinding.lytMsgline.removeAllViews()
                        ClientAsyncTask.ReConnectTask(selected).execute()
                        reconnectDialog.dismiss()
                        headerStartCount.text = "x" + StarPointCounter.decreaseCount(this@MainActivity).toString()
                    }
                    return
                }
                mBinding.lytMsgline.removeAllViews()
                ClientAsyncTask.ReConnectTask(MALE).execute()
                reconnectDialog.dismiss()
            }
        })
    }

    private fun init() {
        // 별포인트 확인.
        StarPointCounter.count(this@MainActivity);
        val starCount = StarPointCounter.getStartCount(this@MainActivity);
        val tvStarCount = mBinding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.tv_star_count)
        tvStarCount.text = "x"+ starCount

        // 리뷰작성 다이얼로그.
        RateItDialogFragment.show(MainActivity@ this, supportFragmentManager)

        mWeakHandler = WeakHandler(Looper.getMainLooper())
        val eventHandler = MainHandler(this, mBinding)
        mBinding.edtMsg.filters = arrayOf<InputFilter>(LengthFilter(150))
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
                    addView(this@MainActivity,msg,null,3)
                }
                mBinding.edtMsg.setText("")
            }

            override fun onClickBtnReload(msg: String?) {
                reconnectDialog.show()
            }
        })
        mBinding.handler = eventHandler
    }

    private fun setNavigation() {
        mBinding.navigationView.setNavigationItemSelectedListener { menuItem ->
            // menuItem.isChecked = true
            when (menuItem.itemId) {
                R.id.navigation_item_notice -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    val prev = supportFragmentManager.findFragmentByTag("NOTICE")
                    if (prev != null) {
                        // back 버튼을 눌렀을 경우 사라지게 됨.
                        fragmentTransaction.remove(prev)
                    }
                    fragmentTransaction.addToBackStack(null)
                    val newFragment = NoticeDialogFragment.newInstance(3)
                    newFragment.show(fragmentTransaction, "NOTICE")
                }

                R.id.navigation_item_store -> {
                    showToast(
                        context = MainActivity@ this,
                        msg = "업데이트 준비 중입니다."
                    )
                }
                R.id.navigation_item_version -> {
                    showToast(
                        context = MainActivity@ this,
                        msg = "Version: " + packageManager.getPackageInfo(
                            packageName,
                            0
                        ).versionName + "v"
                    )
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
                val title = String.format(
                    Locale.getDefault(),
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
            //.setAd(Ad(AdName.ADMOB, AdType.INTERSTITIAL, getString(R.string.admob_interstitial)))
            //.setAd(Ad(AdName.FACEBOOK, AdType.INTERSTITIAL, getString(R.string.facebook_interstitial)))
            .setAd(Ad(AdName.CAULY,AdType.INTERSTITIAL,getString(R.string.cauly)))
            .setOnInterstitialAdLoadListener(object : OnInterstitialAdLoadListener {
                override fun onAdLoaded() {
                    interstitialAd?.showInterstitial()
                }

                override fun onAdFailedToLoad() {}
            })
            .build()
        bottomAd = AdManager.Builder(this@MainActivity)
            .setContainer(mBinding.adContainer)
            //.setAd(Ad(AdName.ADMOB, AdType.BANNER, getString(R.string.admob_banner_bottom)))
            //.setAd(Ad(AdName.FACEBOOK, AdType.BANNER, getString(R.string.facebook_banner_bottom)))
            .setAd(Ad(AdName.CAULY, AdType.BANNER, getString(R.string.cauly)))
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

    private fun setPopupSexDialog() {
        val selectSexDialog = SelectSexDialog(mContext = this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            selectSexDialog.create();
        }
        selectSexDialog.addButtonListener(object :
            SelectSexDialog.Event {
            @Suppress("DEPRECATION")
            override fun onClickMale() {
                // mBinding.scvMsgItem.setBackgroundColor(resources.getColor(R.color.colorChangedScrollViewBackground))
                CURRENT_SEX = MALE
                ClientAsyncTask.ServerConnectTask(this@MainActivity).execute()
                selectSexDialog.dismiss()
            }

            @Suppress("DEPRECATION")
            override fun onClickFemale() {
                CURRENT_SEX = FEMALE
                ClientAsyncTask.ServerConnectTask(this@MainActivity).execute()
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


}