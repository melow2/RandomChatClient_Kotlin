package com.stranger.client.core

import android.os.AsyncTask
import android.os.Looper
import android.util.Log
import android.view.View
import com.stranger.client.core.MessageConstants.MESSAGING
import com.stranger.client.core.MessageConstants.MSG_DELIM
import com.stranger.client.core.MessageConstants.MSG_LEAVE
import com.stranger.client.core.MessageConstants.MSG_REQUIRE_RECONNECT
import com.stranger.client.core.MessageConstants.RE_CONNECT
import com.stranger.client.core.MessageConstants.objectToByteBuffer
import com.stranger.client.core.MessageConstants.parseMessage
import com.stranger.client.core.SocketManager.ROOM_NUMBER
import com.stranger.client.core.SocketManager.exit
import com.stranger.client.core.SocketManager.socketChannel
import com.stranger.client.databinding.MainActivityBinding
import com.stranger.client.util.DataSecurityUtil.getDeviceId
import com.stranger.client.view.activity.MainActivity
import com.stranger.client.view.handler.WeakHandler
import model.SocketClient
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors

object ClientAsyncTask {

    private lateinit var _mContext: MainActivity
    private lateinit var _mBinding: MainActivityBinding
    private lateinit var _mCurrentSex: String
    private lateinit var _weakHandler: WeakHandler

    @Suppress("DEPRECATION")
    class ServerConnectTask(
        mainActivity: MainActivity,
        binding: MainActivityBinding,
        currentSex: String
    ) : AsyncTask<Void?, Void?, Boolean>() {
        init {
            _mContext = mainActivity
            _mBinding = binding
            _mCurrentSex = currentSex
            _weakHandler =
                WeakHandler(Looper.getMainLooper())
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            val executorService =
                Executors.newSingleThreadExecutor()
            val _mChatClient = RandomChatClient(_mContext, _mBinding, _mCurrentSex)
            executorService.execute(_mChatClient)
            return true
        }
    }

    @Suppress("DEPRECATION")
    class SendMessageTask :
        AsyncTask<String?, Void?, Boolean>() {
        override fun doInBackground(vararg msg: String?): Boolean? {
            if (msg[0] != "") {
                try {
                    val socketClient = SocketClient(MESSAGING,getDeviceId(_mContext), _mCurrentSex,ROOM_NUMBER,msg[0],null)
                    socketChannel.write(objectToByteBuffer(socketClient))
                } catch (e: IOException) {
                    addView(MSG_REQUIRE_RECONNECT, 2)
                }
            }
            return true
        }

        private fun addView(msg: String, i: Int) {
            _weakHandler.post(Runnable {
                _mBinding.lytMsgline.addView(RandomChatLog(_mContext, _mBinding, msg, null, i))
                _mBinding.scvMsgItem.post {_mBinding.scvMsgItem.fullScroll(View.FOCUS_DOWN) }
                _mBinding.edtMsg.requestFocus()
            })
        }
    }

    @Suppress("DEPRECATION")
    class ReConnectTask(val selected:String) :
        AsyncTask<Void?, Void?, Boolean>() {
        override fun doInBackground(vararg p0: Void?): Boolean? {
            try {
                val socketClient = SocketClient(RE_CONNECT,getDeviceId(_mContext), _mCurrentSex, ROOM_NUMBER,MSG_LEAVE,selected)
                socketChannel.write(objectToByteBuffer(socketClient))
            } catch (e: IOException) {
                exit()
                addView(MSG_REQUIRE_RECONNECT, 2)
            }
            return true
        }

        private fun addView(msg: String, i: Int) {
            _weakHandler.post(Runnable {
                _mBinding.lytMsgline.addView(RandomChatLog(_mContext, _mBinding, msg, null, i))
                _mBinding.scvMsgItem.post { _mBinding.scvMsgItem.fullScroll(View.FOCUS_DOWN) }
                _mBinding.edtMsg.requestFocus()
            })
        }
    }
}