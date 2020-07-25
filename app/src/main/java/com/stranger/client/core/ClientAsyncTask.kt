package com.stranger.client.core

import android.os.AsyncTask
import com.stranger.client.core.MessageConstants.MESSAGING
import com.stranger.client.core.MessageConstants.MSG_LEAVE
import com.stranger.client.core.MessageConstants.MSG_REQUIRE_RECONNECT
import com.stranger.client.core.MessageConstants.RE_CONNECT
import com.stranger.client.core.MessageConstants.objectToByteBuffer
import com.stranger.client.core.SocketManager.ROOM_NUMBER
import com.stranger.client.core.SocketManager.exit
import com.stranger.client.core.SocketManager.socketChannel
import com.stranger.client.view.activity.MainActivity
import com.stranger.client.view.activity.MainActivity.Companion.CURRENT_SEX
import com.stranger.client.view.activity.MainActivity.Companion.addView
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