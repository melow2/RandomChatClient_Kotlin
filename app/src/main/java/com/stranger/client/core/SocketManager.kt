package com.stranger.client.core

import java.net.SocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

object SocketManager {

    var selector: Selector?=null
    var socketChannel: SocketChannel?=null
    var ROOM_NUMBER:Long =0L

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
            selector?.close()
            socketChannel?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}