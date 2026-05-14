package pw.binom.dns.protocol

import kotlinx.io.Sink
import kotlinx.io.writeUInt
import kotlinx.io.writeUShort
import pw.binom.dns.protocol.utils.DomainName
import pw.binom.dns.protocol.utils.readInt
import pw.binom.dns.protocol.utils.readShort


data class Resource(
    val name: String,
    val type: DnsType,
    val clazz: DnsClass,
    val ttl: UInt,
    val rdata: RData,
) {
    companion object {
        fun read(data: ByteArray, offset: Int): Pair<Resource, Int> {
            val (name, newPos) = DomainName.readDns(data, offset)
            var offset = newPos
            val type = readShort(data, newPos).toUShort()
            offset += Short.SIZE_BYTES
            val clazz = readShort(data, offset).toUShort()
            offset += Short.SIZE_BYTES
            val ttl = readInt(data, offset).toUInt()
            offset += Int.SIZE_BYTES
            val dataSize = readShort(data, offset)
            offset += Short.SIZE_BYTES
            val resource= Resource(
                name = name,
                type = DnsType(type),
                clazz = DnsClass(clazz),
                ttl = ttl,
                rdata = RData(data, offset, dataSize),
            ) to offset + dataSize
            return resource
        }
    }

    fun write(sink: Sink) {
        sink.write(DomainName.write(name))
        sink.writeShort(type.raw.toShort())
        sink.writeShort(clazz.raw.toShort())
        sink.writeUInt(ttl)
        sink.writeUShort(rdata.raw.size.toUInt().toUShort())
        sink.write(rdata.raw)
    }
}
