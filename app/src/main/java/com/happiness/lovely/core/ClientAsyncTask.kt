package com.happiness.lovely.core

import android.os.AsyncTask
import com.happiness.lovely.core.MessageConstants.MESSAGING
import com.happiness.lovely.core.MessageConstants.MSG_LEAVE
import com.happiness.lovely.core.MessageConstants.MSG_REQUIRE_RECONNECT
import com.happiness.lovely.core.MessageConstants.RE_CONNECT
import com.happiness.lovely.core.MessageConstants.objectToByteBuffer
import com.happiness.lovely.core.SocketManager.ROOM_NUMBER
import com.happiness.lovely.core.SocketManager.exit
import com.happiness.lovely.core.SocketManager.socketChannel
import com.happiness.lovely.view.activity.MainActivity
import com.happiness.lovely.view.activity.MainActivity.Companion.CURRENT_SEX
import com.happiness.lovely.view.activity.MainActivity.Companion.addView
import model.SocketClient
import java.io.IOException
import java.util.concurrent.Executors

object ClientAsyncTask {

    private lateinit var _mContext: MainActivity

    @Suppress("DEPRECATION")
    class ServerConnectTask(mContext: MainActivity) : AsyncTask<Void?, Void?, Boolean>() {
        init {
            _mContext = mContext
        }
        override fun doInBackground(vararg p0: Void?): Boolean {
            val executorService = Executors.newSingleThreadExecutor()
            val _mChatClient = RandomChatClient(_mContext,CURRENT_SEX)
            executorService.execute(_mChatClient)
            return true
        }
    }

    @Suppress("DEPRECATION")
    class SendMessageTask : AsyncTask<String?, Void?, Boolean>() {
        override fun doInBackground(vararg msg: String?): Boolean? {
            if (!msg[0].isNullOrEmpty()) {
                try {
                    val socketClient = SocketClient(MESSAGING,"DDDDDD", CURRENT_SEX,ROOM_NUMBER,msg[0],null)
                    socketChannel?.write(objectToByteBuffer(socketClient))
                } catch (e: IOException) {
                    addView(_mContext,MSG_REQUIRE_RECONNECT,null, 2)
                }
            }
            return true
        }
    }

    @Suppress("DEPRECATION")
    class ReConnectTask(val selected:String) : AsyncTask<Void?, Void?, Boolean>() {
        override fun doInBackground(vararg p0: Void?): Boolean? {
            try {
                val socketClient = SocketClient(RE_CONNECT,"DDDDDD", CURRENT_SEX, ROOM_NUMBER,MSG_LEAVE,selected)
                socketChannel?.write(objectToByteBuffer(socketClient))
            } catch (e: IOException) {
                exit()
                addView(_mContext,MSG_REQUIRE_RECONNECT,null,2)
            }
            return true
        }
    }
}