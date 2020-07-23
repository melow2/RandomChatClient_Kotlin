package com.stranger.client.core

import android.os.Looper
import android.view.View
import com.stranger.client.core.BaseClient.IP
import com.stranger.client.core.BaseClient.PORT
import com.stranger.client.core.BaseClient.connectAddress
import com.stranger.client.core.MessageConstants.CONNECTION
import com.stranger.client.core.MessageConstants.MESSAGING
import com.stranger.client.core.MessageConstants.MSG_CONNECT_FAIL
import com.stranger.client.core.MessageConstants.MSG_REQUIRE_RECONNECT
import com.stranger.client.core.MessageConstants.NEW_CLIENT
import com.stranger.client.core.MessageConstants.QUIT_CLIENT
import com.stranger.client.core.MessageConstants.REQUIRE_ACCESS
import com.stranger.client.core.MessageConstants.RE_CONNECT
import com.stranger.client.core.MessageConstants.byteBufferToObject
import com.stranger.client.core.MessageConstants.objectToByteBuffer
import com.stranger.client.core.SocketManager.ROOM_NUMBER
import com.stranger.client.core.SocketManager.disconnect
import com.stranger.client.core.SocketManager.exit
import com.stranger.client.core.SocketManager.selector
import com.stranger.client.core.SocketManager.socketChannel
import com.stranger.client.databinding.MainActivityBinding
import com.stranger.client.util.DataSecurityUtil
import com.stranger.client.util.DataSecurityUtil.getDeviceId
import com.stranger.client.view.activity.MainActivity
import com.stranger.client.view.handler.WeakHandler
import model.SocketClient
import timber.log.Timber
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

open class RandomChatClient(
    private val mContext: MainActivity,
    private val mBinding: MainActivityBinding,
    private val mSex: String
) : Runnable {
    private val mHandler: WeakHandler =
        WeakHandler(Looper.getMainLooper())
    init {
        try {
            connectAddress = InetSocketAddress(
                DataSecurityUtil.decryptText(mContext,IP),
                DataSecurityUtil.decryptText(mContext,PORT)?.toInt()!!)
            selector = Selector.open()
            socketChannel = SocketChannel.open(connectAddress)
            socketChannel.configureBlocking(false)
            socketChannel.register(selector, SelectionKey.OP_READ, StringBuffer())
            val socketClient = SocketClient(REQUIRE_ACCESS,getDeviceId(mContext),mSex);
            socketChannel.write(objectToByteBuffer(socketClient))
        } catch (e: Exception) {
            e.printStackTrace()
            exit()
            addView(MSG_CONNECT_FAIL, null, 2)
        }
    }

    override fun run() {
        try {
            if (selector != null) {
                while (selector.select() > 0) {
                    val keys: MutableIterator<SelectionKey> = selector.selectedKeys().iterator()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        keys.remove()
                        if (!key.isValid) {
                            continue
                        }
                        if (key.isReadable) {
                            receive(key)
                        }
                    }
                }
            } else {
                addView(MSG_CONNECT_FAIL, null, 2)
            }
        } catch (e: IOException) {
        }
    }

    // 수신시 호출 함수.
    private fun receive(key: SelectionKey) {
        val channel = key.channel() as SocketChannel
        val socket = channel.socket()
        val remoteAddr = socket.remoteSocketAddress
        try {
            val readBuffer = ByteBuffer.allocate(1024)
            readBuffer.clear()
            channel.configureBlocking(false) // 채널은 블록킹 상태이기 때문에 논블럭킹 설정.
            val size = channel.read(readBuffer)
            readBuffer.flip()
            if (size == -1) {
                disconnect(channel, key, remoteAddr)
                return
            }
            val client = byteBufferToObject(readBuffer) as SocketClient
            readBuffer.compact()
            messageProcessing(channel, key, client)
        } catch (e: IOException) {
            e.printStackTrace()
            disconnect(channel, key, remoteAddr)
            addView(MSG_REQUIRE_RECONNECT, null, 2)
        }
    }

    protected fun messageProcessing(channel: SocketChannel, key: SelectionKey, client: SocketClient) {
        try {
            ROOM_NUMBER = client.roomNumber
            when (client.protocol) {
                CONNECTION, NEW_CLIENT, QUIT_CLIENT,RE_CONNECT -> {
                    addView(client.message, null, 2)
                }
                MESSAGING -> {
                    mHandler.post { addView(client.message, client.gender, 1) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            disconnect(channel, key, channel.socket().localSocketAddress)
            addView(MSG_REQUIRE_RECONNECT, null, 2)
        }
    }

    private fun addView(msg: String, client_info: String?, i: Int) {
        mHandler.post(Runnable {
            mBinding.lytMsgline.addView(
                RandomChatLog(
                    mContext,
                    mBinding,
                    msg,
                    client_info,
                    i
                )
            )
            mBinding.scvMsgItem.post { mBinding.scvMsgItem.fullScroll(View.FOCUS_DOWN) }
            mBinding.edtMsg.requestFocus()
        })
    }

}