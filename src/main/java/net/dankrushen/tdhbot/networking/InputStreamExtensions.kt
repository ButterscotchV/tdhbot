package net.dankrushen.tdhbot.networking

import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*

fun InputStream.readInt(intBuffer: ByteArray = ByteArray(4)): Int {
    val bytesRead = this.readNBytes(intBuffer, 0, 4)

    if (bytesRead < 0)
        throw NoSuchElementException("Unable to read integer, end of stream encountered")

    return ByteBuffer.wrap(intBuffer).int
}