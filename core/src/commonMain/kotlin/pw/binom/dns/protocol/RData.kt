package pw.binom.dns.protocol

import pw.binom.dns.protocol.utils.DomainName
import kotlin.jvm.JvmInline

class RData(val raw: ByteArray, val offset: Int, val size: Short) {
    val subData
        get() = raw.copyOfRange(fromIndex = offset, toIndex = offset + size)

    companion object {
        fun cname(name: String) = RData(DomainName.write(name), 0, 0)
        fun ns(name: String) = RData(DomainName.write(name), 0, 0)
        fun ptr(name: String) = RData(DomainName.write(name), 0, 0)
    }


    fun cname() = DomainName.readDns(raw, offset).first
    fun ns() = DomainName.readDns(raw, offset).first
    fun ptr() = DomainName.readDns(raw, offset).first
}