package pw.binom.dns.protocol.records

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.writeUByte
import pw.binom.dns.protocol.RData

class CaaRecord(
    val flags: UByte,
    val tag: String,
    val value: String,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0): CaaRecord {
            val flags = data[offset].toUByte()
            val tagLen = data[offset + 1].toUByte().toInt()
            val tag = data.copyOfRange(offset + 2, offset + 2 + tagLen).decodeToString()
            val value = data.copyOfRange(offset + 2 + tagLen, data.size).decodeToString()
            return CaaRecord(
                flags = flags,
                tag = tag,
                value = value,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val tagBytes = tag.encodeToByteArray()
        require(tagBytes.size in 1..15) { "CAA tag length must be 1-15 bytes" }
        val valueBytes = value.encodeToByteArray()
        val buffer = Buffer()
        buffer.writeUByte(flags)
        buffer.writeUByte(tagBytes.size.toUByte())
        buffer.write(tagBytes)
        buffer.write(valueBytes)
        return buffer.readByteArray()
    }
}

fun RData.Companion.caa(record: CaaRecord): RData {
    val data = record.toByteArray()
    return RData(
        raw = data,
        offset = 0,
        size = data.size.toUShort(),
    )
}

fun RData.Companion.caa(
    flags: UByte,
    tag: String,
    value: String,
) = caa(
    CaaRecord(
        flags = flags,
        tag = tag,
        value = value,
    )
)

fun RData.caa() = CaaRecord.from(raw, offset)
