package pw.binom.dns.protocol.records

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import pw.binom.dns.protocol.RData
import pw.binom.dns.protocol.utils.StringUtils

class HINFORecord(
    val cpu: String,
    val os: String,
) {
    companion object {
        fun from(data: ByteArray, offset: Int = 0) {
            val (cpu, pos) = StringUtils.read(data, offset)
            val (os, _) = StringUtils.read(data, pos)
            HINFORecord(
                cpu = cpu,
                os = os,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val buffer = Buffer()
        StringUtils.write(cpu, buffer)
        StringUtils.write(os, buffer)
        return buffer.readByteArray()
    }
}

fun RData.Companion.hinfo(record: HINFORecord) = RData(record.toByteArray(), 0, 0)
fun RData.Companion.hinfo(
    cpu: String,
    os: String,
) = hinfo(
    HINFORecord(
        cpu = cpu,
        os = os,
    )
)

fun RData.hinfo() = HINFORecord.from(raw, offset)