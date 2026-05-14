package pw.binom.dns.protocol.utils

import pw.binom.dns.protocol.DnsType
import pw.binom.dns.protocol.RData
import pw.binom.dns.protocol.Resource
import pw.binom.dns.protocol.records.ipv4
import pw.binom.dns.protocol.records.ipv6
import pw.binom.dns.protocol.records.mx
import pw.binom.dns.protocol.records.soa
import pw.binom.dns.protocol.records.txt

fun Resource.normalizedRdata(): RData? {
    if (rdata.offset == 0 && rdata.size == rdata.raw.size.toShort()) {
        return rdata
    }
    return when (type) {
        DnsType.A -> RData.ipv4(rdata.ipv4())
        DnsType.AAAA -> RData.ipv6(rdata.ipv6())
        DnsType.CNAME -> RData.cname(rdata.cname())
        DnsType.MX -> RData.mx(rdata.mx())
        DnsType.SOA -> RData.soa(rdata.soa())
        DnsType.NS -> RData.ns(rdata.ns())
        DnsType.PTR -> RData.ptr(rdata.ptr())
        DnsType.TXT -> RData.txt(rdata.txt())
        else -> null
    }
}
