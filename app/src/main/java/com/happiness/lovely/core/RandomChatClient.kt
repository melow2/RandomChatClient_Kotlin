package com.happiness.lovely.core

import android.os.Looper
import com.happiness.lovely.core.MessageConstants.CONNECTION
import com.happiness.lovely.core.MessageConstants.MESSAGING
import com.happiness.lovely.core.MessageConstants.MSG_CONNECT_FAIL
import com.happiness.lovely.core.MessageConstants.MSG_REQUIRE_RECONNECT
import com.happiness.lovely.core.MessageConstants.NEW_CLIENT
import com.happiness.lovely.core.MessageConstants.QUIT_CLIENT
import com.happiness.lovely.core.MessageConstants.REQUIRE_ACCESS
import com.happiness.lovely.core.MessageConstants.RE_CONNECT
import com.happiness.lovely.core.MessageConstants.byteBufferToObject
import com.happiness.lovely.core.MessageConstants.objectToByteBuffer
import com.happiness.lovely.core.SocketManager.IP
import com.happiness.lovely.core.SocketManager.PORT
import com.happiness.lovely.core.SocketManager.ROOM_NUMBER
import com.happiness.lovely.core.SocketManager.connectAddress
import com.happiness.lovely.core.SocketManager.disconnect
import com.happiness.lovely.core.SocketManager.exit
import com.happiness.lovely.core.SocketManager.selector
import com.happiness.lovely.core.SocketManager.socketChannel
import com.happiness.lovely.util.DataSecurityUtil.defaultDecryption
import com.happiness.lovely.view.activity.MainActivity
import com.happiness.lovely.view.activity.MainActivity.Companion.addView
import com.happiness.lovely.view.handler.WeakHandler
import model.SocketClient
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class RandomChatClient(
    private val mContext:MainActivity,
    private val mSex: String?) : Runnable {
    private val mHandler: WeakHandler = WeakHandler(Looper.getMainLooper())

    init {
        try {
            connectAddress = InetSocketAddress(defaultDecryption(IP), defaultDecryption(PORT).toInt())
            selector = Selector.open()
            socketChannel = SocketChannel.open(connectAddress)
            socketChannel?.configureBlocking(false)
            socketChannel?.register(selector, SelectionKey.OP_READ, StringBuffer())
            val socketClient = SocketClient(REQUIRE_ACCESS,"getDeviceId(mContext)",mSex);
            socketChannel?.write(objectToByteBuffer(socketClient))
        } catch (e: Exception) {
            e.printStackTrace()
            exit()
            addView(mContext,MSG_CONNECT_FAIL, null, 2)
        }
    }

    override fun run() {
        try {
            if (selector != null) {
                while (selector?.select()!! > 0) {
                    val keys: MutableIterator<SelectionKey> = selector?.selectedKeys()?.iterator()!!
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
                addView(mContext,MSG_CONNECT_FAIL, null, 2)
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
            disconnect(channel, key, remoteAddr)
            addView(mContext, e.toString(), null, 2)
        }
    }

    protected fun messageProcessing(channel: SocketChannel, key: SelectionKey, client: SocketClient) {
        try {
            ROOM_NUMBER = client.roomNumber
            when (client.protocol) {
                CONNECTION, NEW_CLIENT, QUIT_CLIENT,RE_CONNECT -> {
                    addView(mContext,client.message, null, 2)
                }
                MESSAGING -> {
                    mHandler.post { addView(mContext,client.message, client, 1) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            disconnect(channel, key, channel.socket().localSocketAddress)
            addView(mContext,MSG_REQUIRE_RECONNECT, null, 2)
        }
    }
}