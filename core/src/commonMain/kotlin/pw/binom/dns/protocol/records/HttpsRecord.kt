package pw.binom.dns.protocol.records

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import pw.binom.dns.protocol.utils.DomainName
import pw.binom.dns.protocol.RData
import pw.binom.dns.protocol.utils.readShort

class HttpsRecord(
    val priority: UShort,
    val target: String,
    val svcParams: ByteArray,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0): HttpsRecord {
            val priority = readShort(data, offset).toUShort()
            val svcParamsOffset = offset + Short.SIZE_BYTES
            val (target, newPos) = DomainName.readDns(data, svcParamsOffset)
            val svcParams = data.copyOfRange(newPos, data.size)
            return HttpsRecord(
                priority = priority,
                target = target,
                svcParams = svcParams,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val buffer = Buffer()
        buffer.writeShort(priority.toShort())
        buffer.write(DomainName.write(target))
        buffer.write(svcParams)
        return buffer.readByteArray()
    }
}

fun RData.Companion.https(record: HttpsRecord): RData {
    val data = record.toByteArray()
    return RData(
        raw = data,
        offset = 0,
        size = data.size.toUShort(),
    )
}

fun RData.Companion.https(
    priority: UShort,
    target: String,
    svcParams: ByteArray = byteArrayOf(),
) = https(
    HttpsRecord(
        priority = priority,
        target = target,
        svcParams = svcParams,
    )
)

fun RData.https() = HttpsRecord.from(raw, offset)
