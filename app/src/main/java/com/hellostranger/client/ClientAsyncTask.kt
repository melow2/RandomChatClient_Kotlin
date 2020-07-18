package com.hellostranger.client

import android.os.AsyncTask
import android.os.Looper
import android.view.View
import com.hellostranger.client.MessageConstants.MESSAGING
import com.hellostranger.client.MessageConstants.MSG_DELIM
import com.hellostranger.client.MessageConstants.MSG_REQUIRE_RECONNECT
import com.hellostranger.client.MessageConstants.RE_CONNECT
import com.hellostranger.client.MessageConstants.parseMessage
import com.hellostranger.client.SocketManager.ROOM_NUMBER
import com.hellostranger.client.SocketManager.exit
import com.hellostranger.client.SocketManager.socketChannel
import com.hellostranger.client.databinding.MainActivityBinding
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
            _weakHandler = WeakHandler(Looper.getMainLooper())
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            val executorService =
                Executors.newSingleThreadExecutor()
            val _mChatClient = RandomChatClient(
                _mContext,
                _mBinding,
                _mCurrentSex
            )
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
                    val buffer: ByteBuffer = parseMessage(
                        MESSAGING + MSG_DELIM
                                + ROOM_NUMBER + MSG_DELIM
                                + msg[0] + MSG_DELIM
                            .toString() + _mCurrentSex
                    )
                    socketChannel.write(buffer)
                } catch (e: IOException) {
                    addView(MSG_REQUIRE_RECONNECT, 2)
                }
            }
            return true
        }

        private fun addView(msg: String, i: Int) {
            _weakHandler.post(Runnable {
                _mBinding.lytMsgline.addView(
                    RandomChatLog(_mContext, _mBinding, msg, null, i)
                )
                _mBinding.scvMsgItem.post {
                    _mBinding.scvMsgItem.fullScroll(
                        View.FOCUS_DOWN
                    )
                }
                _mBinding.edtMsg.requestFocus()
            })
        }
    }

    @Suppress("DEPRECATION")
    class ReConnectTask :
        AsyncTask<Void?, Void?, Boolean>() {
        override fun doInBackground(vararg p0: Void?): Boolean? {
            try {
                socketChannel.write(parseMessage(RE_CONNECT + MSG_DELIM + ROOM_NUMBER + MSG_DELIM.toString() + "RE_CONNECT"))
            } catch (e: IOException) {
                exit()
                addView(MSG_REQUIRE_RECONNECT, 2)
            }
            return true
        }

        private fun addView(msg: String, i: Int) {
            _weakHandler.post(Runnable {
                _mBinding.lytMsgline.addView(
                    RandomChatLog(_mContext, _mBinding, msg, null, i)
                )
                _mBinding.scvMsgItem.post {
                    _mBinding.scvMsgItem.fullScroll(
                        View.FOCUS_DOWN
                    )
                }
                _mBinding.edtMsg.requestFocus()
            })
        }
    }
}