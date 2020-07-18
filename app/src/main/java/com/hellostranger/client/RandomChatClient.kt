package com.hellostranger.client

import android.os.Looper
import android.view.View
import com.hellostranger.client.BaseClient.IP
import com.hellostranger.client.BaseClient.PORT
import com.hellostranger.client.BaseClient.connectAddress
import com.hellostranger.client.MessageConstants.CONNECTION
import com.hellostranger.client.MessageConstants.MESSAGING
import com.hellostranger.client.MessageConstants.MSG_CONNECT_FAIL
import com.hellostranger.client.MessageConstants.MSG_DELIM
import com.hellostranger.client.MessageConstants.MSG_REQUIRE_RECONNECT
import com.hellostranger.client.MessageConstants.NEW_CLIENT
import com.hellostranger.client.MessageConstants.QUIT_CLIENT
import com.hellostranger.client.MessageConstants.REQUIRE_ACCESS
import com.hellostranger.client.MessageConstants.RE_CONNECT
import com.hellostranger.client.MessageConstants.encoder
import com.hellostranger.client.SocketManager.ROOM_NUMBER
import com.hellostranger.client.SocketManager.disconnect
import com.hellostranger.client.SocketManager.selector
import com.hellostranger.client.SocketManager.socketChannel
import com.hellostranger.client.databinding.MainActivityBinding

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.*

open class RandomChatClient(
    private val mContext: MainActivity,
    private val mBinding: MainActivityBinding,
    private val mSex: String
) : Runnable {
    private val mHandler: WeakHandler = WeakHandler(Looper.getMainLooper())
    init {
        try {
            connectAddress = InetSocketAddress(AES256Util.decryption(IP),AES256Util.decryption(PORT)?.toInt()!!)
            selector = Selector.open()
            socketChannel = SocketChannel.open(connectAddress)
            socketChannel.configureBlocking(false)
            socketChannel.register(selector, SelectionKey.OP_READ, StringBuffer())
            socketChannel.write(encoder.encode(CharBuffer.wrap(REQUIRE_ACCESS + MSG_DELIM.toString() + mSex)))
        } catch (e: Exception) {
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
            val readBuffer = ByteBuffer.allocate(1024 * 100)
            readBuffer.clear()
            channel.configureBlocking(false) // 채널은 블록킹 상태이기 때문에 논블럭킹 설정.
            val size = channel.read(readBuffer)
            readBuffer.flip()
            if (size == -1) {
                disconnect(channel, key, remoteAddr)
                return
            }
            val data = ByteArray(size)
            System.arraycopy(readBuffer.array(), 0, data, 0, size)
            val received = String(data, charset("UTF-8"))
            messageProcessing(channel, key, received)
        } catch (e: IOException) {
            disconnect(channel, key, remoteAddr)
            addView(MSG_REQUIRE_RECONNECT, null, 2)
        }
    }

    protected fun messageProcessing(
        channel: SocketChannel,
        key: SelectionKey,
        received: String?
    ) {
        try {
            val tokenizer = StringTokenizer(received, MSG_DELIM)
            val protocol = tokenizer.nextToken()
            ROOM_NUMBER = tokenizer.nextToken()
            var message = ""
            when (protocol) {
                CONNECTION, NEW_CLIENT, QUIT_CLIENT, RE_CONNECT -> {
                    message = tokenizer.nextToken()
                    addView(message, null, 2)
                }
                MESSAGING -> {
                    val msg = tokenizer.nextToken()
                    val clientInfo = tokenizer.nextToken()
                    mHandler.post(Runnable { addView(msg, clientInfo, 1) })
                    while (tokenizer.hasMoreTokens()) {
                        val rProtocol = tokenizer.nextToken()
                        val rRoomNumber = tokenizer.nextToken()
                        val rMsg = tokenizer.nextToken()
                        val rClientInfo = tokenizer.nextToken()
                        mHandler.post(Runnable { addView(rMsg, rClientInfo, 1) })
                    }
                }
            }
        } catch (e: Exception) {
            disconnect(channel, key, channel.socket().localSocketAddress)
            addView(MSG_REQUIRE_RECONNECT, null, 2)
        }
    }

    private fun addView(msg: String, client_info: String?, i: Int) {
        mHandler.post(Runnable {
            mBinding.lytMsgline.addView(RandomChatLog(mContext, mBinding, msg, client_info, i))
            mBinding.scvMsgItem.post { mBinding.scvMsgItem.fullScroll(View.FOCUS_DOWN) }
            mBinding.edtMsg.requestFocus()
        })
    }

}