package com.stranger.client.core

import java.net.SocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

object SocketManager {

    lateinit var selector: Selector
    lateinit var socketChannel: SocketChannel
    lateinit var ROOM_NUMBER:String

    internal fun disconnect(
        channel: SocketChannel,
        key: SelectionKey,
        addr: SocketAddress?
    ) {
        try {
            channel.socket().close()
            channel.close()
            key.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exit() {
        try {
            selector!!.close()
            socketChannel!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}