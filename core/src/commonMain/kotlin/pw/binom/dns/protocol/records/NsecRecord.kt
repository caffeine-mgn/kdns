package pw.binom.dns.protocol.records

import pw.binom.dns.protocol.utils.DomainName
import pw.binom.dns.protocol.RData

/**
 * NSEC Record (RFC 4034, Section 4).
 *
 * Points to the next domain name in the zone and lists the record types
 * that exist at the current owner name.
 */
class NsecRecord(
    val nextDomainName: String,
    val typeBitMaps: ByteArray,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0): NsecRecord {
            val (nextDomainName, newPos) = DomainName.readDns(data, offset)
            val typeBitMaps = data.copyOfRange(newPos, data.size)
            return NsecRecord(
                nextDomainName = nextDomainName,
                typeBitMaps = typeBitMaps,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val result = mutableListOf<Byte>()
        result.addAll(DomainName.write(nextDomainName).toList())
        result.addAll(typeBitMaps.toList())
        return result.toByteArray()
    }
}

fun RData.Companion.nsec(record: NsecRecord): RData {
    val data = record.toByteArray()
    return RData(data, 0, data.size.toUShort())
}

fun RData.nsec() = NsecRecord.from(raw, offset)
