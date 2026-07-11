package pw.binom.dns.protocol.records

import pw.binom.dns.protocol.RData

/**
 * EDNS(0) OPT pseudo-record (RFC 6891).
 *
 * Appears in the additional section of a DNS message.
 * The existing [pw.binom.dns.protocol.Resource] parser already handles OPT's
 * wire format (name=root → "", type=41, class=UDP payload size,
 * ttl=packed flags, rdata=options). This class provides typed access to
 * the OPT-specific fields packed inside [Resource.clazz] and [Resource.ttl].
 */
class OptRecord(
    val udpPayloadSize: UShort,
    val extendedRcode: UByte,
    val version: UByte,
    val doBit: Boolean,
    val options: ByteArray,
) {
    companion object {
        /**
         * Builds an OptRecord from the packed fields of a parsed Resource.
         * @param clazz  the CLASS field of the OPT record (= UDP payload size)
         * @param ttl    the TTL field containing EXTENDED-RCODE | VERSION | DO | Z
         * @param rdata  the RDATA containing EDNS options
         */
        fun from(clazz: UShort, ttl: UInt, rdata: RData): OptRecord {
            val extendedRcode = ((ttl shr 24) and 0xFFu).toUByte()
            val version = ((ttl shr 16) and 0xFFu).toUByte()
            val doBit = ((ttl shr 15) and 1u) == 1u
            return OptRecord(
                udpPayloadSize = clazz,
                extendedRcode = extendedRcode,
                version = version,
                doBit = doBit,
                options = rdata.subData,
            )
        }
    }

    /**
     * Packs the EXTENDED-RCODE, VERSION, DO and Z fields into a 32-bit TTL value
     * suitable for storing in [pw.binom.dns.protocol.Resource.ttl].
     */
    fun packedTtl(): UInt {
        return (extendedRcode.toUInt() shl 24) or
                (version.toUInt() shl 16) or
                (if (doBit) 1u shl 15 else 0u)
    }
}

fun RData.opt(): OptRecord {
    // Not used directly from RData — OptRecord is built from Resource fields.
    // Kept for consistency with other record parsers.
    error("Use OptRecord.from(clazz, ttl, rdata) instead")
}
