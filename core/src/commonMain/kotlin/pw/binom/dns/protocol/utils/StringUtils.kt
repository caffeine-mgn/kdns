package pw.binom.dns.protocol.utils

import io.ktor.utils.io.core.writeFully
import kotlinx.io.Sink
import kotlinx.io.writeUByte

internal object StringUtils {
    fun read(data: ByteArray, offset: Int): Pair<String, Int> {
        val size = data[offset].toUByte().toInt()
        val result = data.copyOfRange(offset + 1, offset + 1 + size).decodeToString()
        return result to (1 + size + offset)
    }

    fun write(data: String, sink: Sink) {
        val bytes = data.encodeToByteArray()
        var pos = 0
        while (true) {
            val rem = bytes.size - pos
            if (rem <= 0) {
                break
            }
            val l = minOf(bytes.size - pos, 255)
            sink.writeUByte(l.toUByte())
            sink.writeFully(bytes, offset = pos, length = l)
            pos += l
        }
    }
}