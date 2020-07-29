package com.happiness.lovely.core

import java.io.*
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CharsetEncoder

object MessageConstants {

    const val REQUIRE_ACCESS = "REQUIRE_ACCESS"
    const val MESSAGING = "MESSAGING"
    const val CONNECTION = "CONNECTION"
    const val NEW_CLIENT = "NEW_CLIENT"
    const val QUIT_CLIENT = "QUIT_CLIENT"
    const val RE_CONNECT = "RE_CONNECT"
    const val MSG_CONNECT_FAIL = "현재 서버에 사용자가 많습니다."
    const val MSG_REQUIRE_RECONNECT = "서버와 연결이 끊어졌습니다. 앱을 다시 실행 해주세요"
    const val MSG_ERR_LOG_RECONNECT = "장시간 접속하지 않아, 연결이 끊어졌습니다."
    const val MSG_LEAVE = "낯선 사람이 떠났습니다."
    const val SKSP="FLOWERKDSAPEXMDIIQ)#DKWS(#JDCJDI"
    const val MALE = "M"
    const val FEMALE = "F"
    const val RANDOM = "R"

    val charset = Charsets.UTF_8
    var encoder: CharsetEncoder = charset.newEncoder()

    @Throws(CharacterCodingException::class)
    fun parseMessage(msg: String?): ByteBuffer {
        var buffer = ByteBuffer.allocate(1024 * 100)
        buffer.clear()
        buffer = encoder.encode(CharBuffer.wrap(msg))
        return buffer
    }

    @Throws(java.lang.Exception::class)
    fun byteBufferToObject(byteBuffer: ByteBuffer): Any? {
        val bytes = ByteArray(byteBuffer.limit())
        byteBuffer[bytes]
        return deSerializer(bytes)
    }

    @Throws(Exception::class)
    fun objectToByteBuffer(`object`: Any?): ByteBuffer? {
        var byteBuf: ByteBuffer? = null
        byteBuf = ByteBuffer.wrap(serializer(`object`))
        byteBuf.rewind()
        return byteBuf
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun deSerializer(bytes: ByteArray?): Any {
        val objectInputStream = ObjectInputStream(ByteArrayInputStream(bytes))
        return objectInputStream.readObject()
    }

    @Throws(IOException::class)
    fun serializer(`object`: Any?): ByteArray? {
        var objectOutputStream: ObjectOutputStream? = null
        val byteArrayOutputStream: ByteArrayOutputStream
        return try {
            byteArrayOutputStream = ByteArrayOutputStream()
            objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(`object`)
            objectOutputStream.flush()
            byteArrayOutputStream.toByteArray()
        } catch (e: IOException) {
            throw e
        } finally {
            if (objectOutputStream != null) {
                objectOutputStream.close()
            }
        }
    }
}