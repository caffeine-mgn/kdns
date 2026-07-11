package pw.binom.dns.protocol.records

import pw.binom.dns.protocol.RData

/**
 * NSEC3 Record (RFC 5155, Section 3).
 *
 * Provides authenticated denial of existence for DNSSEC, using hashed
 * owner names to prevent zone enumeration.
 */
class Nsec3Record(
    val hashAlgorithm: UByte,
    val flags: UByte,
    val iterations: UShort,
    val salt: ByteArray,
    val nextHashedOwnerName: ByteArray,
    val typeBitMaps: ByteArray,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0): Nsec3Record {
            var pos = offset
            val hashAlgorithm = data[pos].toUByte()
            pos += 1
            val flags = data[pos].toUByte()
            pos += 1
            val iterations = ((data[pos].toInt() and 0xFF) shl 8 or (data[pos + 1].toInt() and 0xFF)).toUShort()
            pos += 2
            val saltLength = data[pos].toUByte().toInt()
            pos += 1
            val salt = if (saltLength > 0) data.copyOfRange(pos, pos + saltLength) else byteArrayOf()
            pos += saltLength
            val hashLength = data[pos].toUByte().toInt()
            pos += 1
            val nextHashedOwnerName = data.copyOfRange(pos, pos + hashLength)
            pos += hashLength
            val typeBitMaps = data.copyOfRange(pos, data.size)
            return Nsec3Record(
                hashAlgorithm = hashAlgorithm,
                flags = flags,
                iterations = iterations,
                salt = salt,
                nextHashedOwnerName = nextHashedOwnerName,
                typeBitMaps = typeBitMaps,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val result = mutableListOf<Byte>()
        result.add(hashAlgorithm.toByte())
        result.add(flags.toByte())
        result.add((iterations.toInt() shr 8).toByte())
        result.add(iterations.toInt().toByte())
        result.add(salt.size.toByte())
        result.addAll(salt.toList())
        result.add(nextHashedOwnerName.size.toByte())
        result.addAll(nextHashedOwnerName.toList())
        result.addAll(typeBitMaps.toList())
        return result.toByteArray()
    }
}

fun RData.Companion.nsec3(record: Nsec3Record): RData {
    val data = record.toByteArray()
    return RData(data, 0, data.size.toUShort())
}

fun RData.nsec3() = Nsec3Record.from(raw, offset)
