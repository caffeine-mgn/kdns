package pw.binom.dns.protocol.records

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import pw.binom.dns.protocol.utils.DomainName
import pw.binom.dns.protocol.RData
import pw.binom.dns.protocol.utils.readShort

class MxRecord(
    val preference: UShort,
    val exchange: String,
) {
    companion object {
        fun from(data: ByteArray) = MxRecord(
            preference = readShort(data, 0).toUShort(),
            exchange = DomainName.readDns(data, Short.SIZE_BYTES).first,
        )
    }

    fun toByteArray(): ByteArray {
        val buffer = Buffer()
        buffer.writeShort(preference.toShort())
        buffer.write(DomainName.write(exchange))
        return buffer.readByteArray()
    }
}

fun RData.Companion.mx(record: MxRecord) = RData(record.toByteArray(), 0, 0)
fun RData.Companion.mx(
    preference: UShort,
    exchange: String,
) = mx(
    MxRecord(
        preference = preference,
        exchange = exchange,
    )
)

fun RData.mx() = MxRecord.from(raw)