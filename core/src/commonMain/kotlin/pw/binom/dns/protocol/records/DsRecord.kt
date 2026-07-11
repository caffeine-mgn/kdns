package pw.binom.dns.protocol.records

import pw.binom.dns.protocol.RData

/**
 * DS Record (RFC 4034, Section 5).
 *
 * Delegation Signer — identifies a DNSSEC signing key of a delegated zone.
 */
class DsRecord(
    val keyTag: UShort,
    val algorithm: UByte,
    val digestType: UByte,
    val digest: ByteArray,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0): DsRecord {
            val keyTag = ((data[offset].toInt() and 0xFF) shl 8 or (data[offset + 1].toInt() and 0xFF)).toUShort()
            val algorithm = data[offset + 2].toUByte()
            val digestType = data[offset + 3].toUByte()
            val digest = data.copyOfRange(offset + 4, data.size)
            return DsRecord(
                keyTag = keyTag,
                algorithm = algorithm,
                digestType = digestType,
                digest = digest,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val result = mutableListOf<Byte>()
        result.add((keyTag.toInt() shr 8).toByte())
        result.add(keyTag.toInt().toByte())
        result.add(algorithm.toByte())
        result.add(digestType.toByte())
        result.addAll(digest.toList())
        return result.toByteArray()
    }
}

fun RData.Companion.ds(record: DsRecord): RData {
    val data = record.toByteArray()
    return RData(data, 0, data.size.toUShort())
}

fun RData.ds() = DsRecord.from(raw, offset)
