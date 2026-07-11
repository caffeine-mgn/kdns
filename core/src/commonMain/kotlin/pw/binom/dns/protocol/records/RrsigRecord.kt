package pw.binom.dns.protocol.records

import pw.binom.dns.protocol.utils.DomainName
import pw.binom.dns.protocol.RData

/**
 * RRSIG Record (RFC 4034, Section 3).
 *
 * DNSSEC signature for an RRset.
 */
class RrsigRecord(
    val typeCovered: UShort,
    val algorithm: UByte,
    val labels: UByte,
    val originalTtl: UInt,
    val signatureExpiration: UInt,
    val signatureInception: UInt,
    val keyTag: UShort,
    val signerName: String,
    val signature: ByteArray,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0): RrsigRecord {
            var pos = offset
            val typeCovered = ((data[pos].toInt() and 0xFF) shl 8 or (data[pos + 1].toInt() and 0xFF)).toUShort()
            pos += 2
            val algorithm = data[pos].toUByte()
            pos += 1
            val labels = data[pos].toUByte()
            pos += 1
            val originalTtl = ((data[pos].toInt() and 0xFF).toUInt() shl 24) or
                    ((data[pos + 1].toInt() and 0xFF).toUInt() shl 16) or
                    ((data[pos + 2].toInt() and 0xFF).toUInt() shl 8) or
                    (data[pos + 3].toInt() and 0xFF).toUInt()
            pos += 4
            val signatureExpiration = ((data[pos].toInt() and 0xFF).toUInt() shl 24) or
                    ((data[pos + 1].toInt() and 0xFF).toUInt() shl 16) or
                    ((data[pos + 2].toInt() and 0xFF).toUInt() shl 8) or
                    (data[pos + 3].toInt() and 0xFF).toUInt()
            pos += 4
            val signatureInception = ((data[pos].toInt() and 0xFF).toUInt() shl 24) or
                    ((data[pos + 1].toInt() and 0xFF).toUInt() shl 16) or
                    ((data[pos + 2].toInt() and 0xFF).toUInt() shl 8) or
                    (data[pos + 3].toInt() and 0xFF).toUInt()
            pos += 4
            val keyTag = ((data[pos].toInt() and 0xFF) shl 8 or (data[pos + 1].toInt() and 0xFF)).toUShort()
            pos += 2
            val (signerName, newPos) = DomainName.readDns(data, pos)
            val signature = data.copyOfRange(newPos, data.size)
            return RrsigRecord(
                typeCovered = typeCovered,
                algorithm = algorithm,
                labels = labels,
                originalTtl = originalTtl,
                signatureExpiration = signatureExpiration,
                signatureInception = signatureInception,
                keyTag = keyTag,
                signerName = signerName,
                signature = signature,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val result = mutableListOf<Byte>()
        result.add((typeCovered.toInt() shr 8).toByte())
        result.add(typeCovered.toInt().toByte())
        result.add(algorithm.toByte())
        result.add(labels.toByte())
        result.add((originalTtl.toInt() shr 24).toByte())
        result.add((originalTtl.toInt() shr 16).toByte())
        result.add((originalTtl.toInt() shr 8).toByte())
        result.add(originalTtl.toInt().toByte())
        result.add((signatureExpiration.toInt() shr 24).toByte())
        result.add((signatureExpiration.toInt() shr 16).toByte())
        result.add((signatureExpiration.toInt() shr 8).toByte())
        result.add(signatureExpiration.toInt().toByte())
        result.add((signatureInception.toInt() shr 24).toByte())
        result.add((signatureInception.toInt() shr 16).toByte())
        result.add((signatureInception.toInt() shr 8).toByte())
        result.add(signatureInception.toInt().toByte())
        result.add((keyTag.toInt() shr 8).toByte())
        result.add(keyTag.toInt().toByte())
        result.addAll(DomainName.write(signerName).toList())
        result.addAll(signature.toList())
        return result.toByteArray()
    }
}

fun RData.Companion.rrsig(record: RrsigRecord): RData {
    val data = record.toByteArray()
    return RData(data, 0, data.size.toUShort())
}

fun RData.rrsig() = RrsigRecord.from(raw, offset)
