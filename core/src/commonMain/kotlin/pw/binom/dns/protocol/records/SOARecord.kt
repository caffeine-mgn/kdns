package pw.binom.dns.protocol.records

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.writeUInt
import pw.binom.dns.protocol.utils.DomainName
import pw.binom.dns.protocol.RData
import pw.binom.dns.protocol.utils.readInt

data class SOARecord(
    val mname: String,
    val rname: String,
    val serial: UInt,
    val refresh: UInt,
    val retry: UInt,
    val expire: UInt,
    val minimum: UInt,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0): SOARecord {
            val (mname, p1) = DomainName.readDns(data, offset)
            val (rname, p2) = DomainName.readDns(data, p1)
            var pos = p2
            val serial = readInt(data, pos).toUInt()
            pos += Int.SIZE_BYTES
            val refresh = readInt(data, pos).toUInt()
            pos += Int.SIZE_BYTES
            val retry = readInt(data, pos).toUInt()
            pos += Int.SIZE_BYTES
            val expire = readInt(data, pos).toUInt()
            pos += Int.SIZE_BYTES
            val minimum = readInt(data, pos).toUInt()
            return SOARecord(
                mname = mname,
                rname = rname,
                serial = serial,
                refresh = refresh,
                retry = retry,
                expire = expire,
                minimum = minimum,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val buffer = Buffer()
        buffer.write(DomainName.write(mname))
        buffer.write(DomainName.write(rname))
        buffer.writeUInt(serial)
        buffer.writeUInt(refresh)
        buffer.writeUInt(retry)
        buffer.writeUInt(expire)
        buffer.writeUInt(minimum)
        return buffer.readByteArray()
    }
}

fun RData.Companion.soa(source: SOARecord) = RData(source.toByteArray(), 0, 0)
fun RData.soa() = SOARecord.from(raw, offset)