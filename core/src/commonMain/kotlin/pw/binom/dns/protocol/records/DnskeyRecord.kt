package pw.binom.dns.protocol.records

import pw.binom.dns.protocol.RData

/**
 * DNSKEY Record (RFC 4034, Section 2).
 *
 * Holds a DNSSEC public key.
 */
class DnskeyRecord(
    val flags: UShort,
    val protocol: UByte,
    val algorithm: UByte,
    val publicKey: ByteArray,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0): DnskeyRecord {
            val flags = ((data[offset].toInt() and 0xFF) shl 8 or (data[offset + 1].toInt() and 0xFF)).toUShort()
            val protocol = data[offset + 2].toUByte()
            val algorithm = data[offset + 3].toUByte()
            val publicKey = data.copyOfRange(offset + 4, data.size)
            return DnskeyRecord(
                flags = flags,
                protocol = protocol,
                algorithm = algorithm,
                publicKey = publicKey,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val result = mutableListOf<Byte>()
        result.add((flags.toInt() shr 8).toByte())
        result.add(flags.toInt().toByte())
        result.add(protocol.toByte())
        result.add(algorithm.toByte())
        result.addAll(publicKey.toList())
        return result.toByteArray()
    }
}

fun RData.Companion.dnskey(record: DnskeyRecord): RData {
    val data = record.toByteArray()
    return RData(data, 0, data.size.toUShort())
}

fun RData.dnskey() = DnskeyRecord.from(raw, offset)
