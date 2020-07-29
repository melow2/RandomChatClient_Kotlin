package com.happiness.lovely.core

import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

object SocketManager {
    const val IP = "Nh8obA99454bjNL+S0pHoA=="
    const val PORT = "xiqqq3/oYVlgCtJWAHXGlQ=="
    var connectAddress: InetSocketAddress? = null
    var selector: Selector? = null
    var socketChannel: SocketChannel? = null
    var ROOM_NUMBER: Long = 0L

    fun disconnect(
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