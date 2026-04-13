package pw.binom.dns.protocol.records

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import pw.binom.dns.protocol.RData
import pw.binom.dns.protocol.utils.StringUtils

fun RData.Companion.txt(data: String): RData {
    val buffer = Buffer()
    StringUtils.write(data, buffer)
    return RData(buffer.readByteArray(), 0, 0)
}

fun RData.txt(): String {
    var pos = offset
    val result = StringBuilder()
    while (pos < offset + size) {
        val (str, newPos) = StringUtils.read(raw, pos)
        pos = newPos
        result.append(str)
    }
    return result.toString()
}