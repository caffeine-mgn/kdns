package pw.binom.dns.protocol.records

import pw.binom.dns.protocol.RData

fun RData.Companion.ipv4(data: ByteArray, offset: Int = 0): RData {
    check(offset + 4 <= data.size) { "Invalid ipv4 address: ${data.toHexString()}" }
    return ipv4(
        data[0].toUByte(),
        data[1].toUByte(),
        data[2].toUByte(),
        data[3].toUByte(),
    )
}

fun RData.Companion.ipv4(b1: UByte, b2: UByte, b3: UByte, b4: UByte) =
    RData(byteArrayOf(b1.toByte(), b2.toByte(), b3.toByte(), b4.toByte()), 0, 0)

fun RData.Companion.ipv4(address: String): RData {
    val items = address.split(".")
    check(items.size == 4) { "Invalid ipv4 address: $address" }
    return ipv4(
        items[0].toUByte(),
        items[1].toUByte(),
        items[2].toUByte(),
        items[3].toUByte(),
    )
}

fun RData.ipv4(): String {
    val raw=subData
    check(raw.size == 4) { "Invalid ipv4 size: ${raw.size}" }
    return "${raw[0].toUByte()}.${raw[1].toUByte()}.${raw[2].toUByte()}.${raw[3].toUByte()}"
}