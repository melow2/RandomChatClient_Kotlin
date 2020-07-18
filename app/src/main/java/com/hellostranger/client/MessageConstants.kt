package com.hellostranger.client

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharacterCodingException

import java.nio.charset.CharsetEncoder

object MessageConstants {

    const val REQUIRE_ACCESS = "REQUIRE_ACCESS"
    const val MESSAGING = "MESSAGING"
    const val CONNECTION = "CONNECTION"
    const val MSG_DELIM = "/"
    const val NEW_CLIENT = "NEW_CLIENT"
    const val QUIT_CLIENT = "QUIT_CLIENT"
    const val RE_CONNECT = "RE_CONNECT"
    const val MSG_CONNECT_FAIL = "현재 서버에 사용자가 많습니다."
    const val MSG_REQUIRE_RECONNECT = "서버와 연결이 끊어졌습니다. 앱을 다시 실행 해주세요"
    const val MSG_BUSY = "현재 사용자가 많습니다"
    val charset = Charsets.UTF_8
    var encoder: CharsetEncoder = charset.newEncoder()

    @Throws(CharacterCodingException::class)
    fun parseMessage(msg: String?): ByteBuffer {
        var buffer = ByteBuffer.allocate(1024 * 100)
        buffer.clear()
        buffer = encoder.encode(CharBuffer.wrap(msg))
        return buffer
    }
}