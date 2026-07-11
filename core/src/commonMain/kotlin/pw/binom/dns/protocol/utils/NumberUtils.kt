package pw.binom.dns.protocol.utils

fun readInt(data: ByteArray, offset: Int): Int {
    require(offset + 4 <= data.size) {
        "readInt: offset=$offset, need 4 bytes, but array size=${data.size}"
    }
    return ((data[offset].toInt() and 0xFF) shl 24) or
            ((data[offset + 1].toInt() and 0xFF) shl 16) or
            ((data[offset + 2].toInt() and 0xFF) shl 8) or
            (data[offset + 3].toInt() and 0xFF)
}

fun readShort(data: ByteArray, offset: Int): Short {
    require(offset + 2 <= data.size) {
        "readShort: offset=$offset, need 2 bytes, but array size=${data.size}"
    }
    return (((data[offset].toInt() and 0xFF) shl 8) or
            (data[offset + 1].toInt() and 0xFF)
            ).toShort()
}
