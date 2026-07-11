package pw.binom.dns.protocol.records

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import pw.binom.dns.protocol.utils.DomainName
import pw.binom.dns.protocol.RData
import pw.binom.dns.protocol.utils.readShort

class SrvRecord(
    val priority: UShort,
    val weight: UShort,
    val port: UShort,
    val target: String,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0) = SrvRecord(
            priority = readShort(data, offset).toUShort(),
            weight = readShort(data, offset + Short.SIZE_BYTES).toUShort(),
            port = readShort(data, offset + Short.SIZE_BYTES * 2).toUShort(),
            target = DomainName.readDns(data, offset + Short.SIZE_BYTES * 3).first,
        )
    }

    fun toByteArray(): ByteArray {
        val buffer = Buffer()
        buffer.writeShort(priority.toShort())
        buffer.writeShort(weight.toShort())
        buffer.writeShort(port.toShort())
        buffer.write(DomainName.write(target))
        return buffer.readByteArray()
    }
}

fun RData.Companion.srv(record: SrvRecord): RData {
    val data = record.toByteArray()
    return RData(
        raw = data,
        offset = 0,
        size = data.size.toUShort(),
    )
}

fun RData.Companion.srv(
    priority: UShort,
    weight: UShort,
    port: UShort,
    target: String,
) = srv(
    SrvRecord(
        priority = priority,
        weight = weight,
        port = port,
        target = target,
    )
)

fun RData.srv() = SrvRecord.from(raw, offset)
