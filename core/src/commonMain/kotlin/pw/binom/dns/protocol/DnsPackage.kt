package pw.binom.dns.protocol

import kotlinx.io.Sink
import kotlinx.io.writeUShort
import pw.binom.dns.protocol.utils.readShort

data class DnsPackage(
    val header: DnsHeader,
    val queries: List<Question>,
    val answer: List<Resource>,
    val authority: List<Resource>,
    val additional: List<Resource>,
) {
    companion object {
        fun read(data: ByteArray): DnsPackage {
            var pos = 0
            val header = DnsHeader.read(data, offset = pos)
            pos += DnsHeader.SIZE_BYTES
            val qdcount = readShort(data, pos)
            pos += Short.SIZE_BYTES
            val ancount = readShort(data, pos)
            pos += Short.SIZE_BYTES
            val nscount = readShort(data, pos)
            pos += Short.SIZE_BYTES
            val arcount = readShort(data, pos)
            pos += Short.SIZE_BYTES

            val queries = (0 until qdcount).map {
                val (q, newPos) = Question.read(data, pos)
                pos = newPos
                q
            }

            val answer = (0 until ancount).map {
                val (q, newPos) = Resource.read(data, pos)
                pos = newPos
                q
            }
            val authority = (0 until nscount).map {
                val (q, newPos) = Resource.read(data, pos)
                pos = newPos
                q
            }
            val additional = (0 until arcount).map {
                val (q, newPos) = Resource.read(data, pos)
                pos = newPos
                q
            }
            return DnsPackage(
                header = header,
                queries = queries,
                answer = answer,
                authority = authority,
                additional = additional,
            )
        }
    }

    fun write(sink: Sink) {
        sink.writeInt(header.raw)
        sink.writeUShort(queries.size.toUInt().toUShort())
        sink.writeUShort(answer.size.toUInt().toUShort())
        sink.writeUShort(authority.size.toUInt().toUShort())
        sink.writeUShort(additional.size.toUInt().toUShort())
        queries.forEach {
            it.write(sink)
        }
        answer.forEach {
            it.write(sink)
        }
        authority.forEach {
            it.write(sink)
        }
        additional.forEach {
            it.write(sink)
        }
    }
}