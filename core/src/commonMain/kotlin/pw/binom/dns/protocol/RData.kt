package pw.binom.dns.protocol

import pw.binom.dns.protocol.utils.DomainName
import kotlin.jvm.JvmInline

class RData(val raw: ByteArray, val offset: Int, val size: Short) {
    val subData
        get() = raw.copyOfRange(fromIndex = offset, toIndex = offset + size)

    companion object {
        fun cname(name: String): RData {
            val data = DomainName.write(name)
            return RData(
                raw = data,
                offset = 0,
                size = data.size.toShort(),
            )
        }

        fun ns(name: String): RData {
            val data = DomainName.write(name)
            return RData(
                raw = data,
                offset = 0,
                size = data.size.toShort(),
            )
        }

        fun ptr(name: String): RData {
            val data = DomainName.write(name)
            return RData(
                raw = data,
                offset = 0,
                size = data.size.toShort(),
            )
        }
    }


    fun cname() = DomainName.readDns(raw, offset).first
    fun ns() = DomainName.readDns(raw, offset).first
    fun ptr() = DomainName.readDns(raw, offset).first
}