package pw.binom.dns.protocol

import kotlinx.io.Sink
import pw.binom.dns.protocol.utils.DomainName
import pw.binom.dns.protocol.utils.readShort

data class Question(
    val name: String,
    val type: DnsType,
    val clazz: DnsClass,
) {
    companion object {
        fun read(source: ByteArray, offset: Int): Pair<Question, Int> {
            val (name, newPos) = DomainName.readDns(source, offset)
            val type = readShort(source, newPos).toUShort()
            val clazz = readShort(source, newPos + Short.SIZE_BYTES).toUShort()
            return Pair(
                Question(name = name, type = DnsType(type), clazz = DnsClass(clazz)),
                newPos + Short.SIZE_BYTES * 2,
            )
        }
    }

    fun write(sink: Sink) {
        sink.write(DomainName.write(name))
        sink.writeShort(type.raw.toShort())
        sink.writeShort(clazz.raw.toShort())
    }
}