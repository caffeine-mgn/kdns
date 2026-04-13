package pw.binom.dns.protocol.records

import pw.binom.dns.protocol.RData

fun RData.ipv6(): String {
    val raw = subData
    check(raw.size == 16) { "Invalid ipv6 size: ${raw.size}" }
    val sb = StringBuilder()
    raw.forEachIndexed { index, byte ->
        sb.append(byte.toUByte().toString(16).padStart(2, '0'))
        if (index < 15 && index % 2 == 1) {
            sb.append(":")
        }
    }
    return sb.toString()
}

fun RData.Companion.ipv6(
    b1: UByte, b2: UByte, b3: UByte, b4: UByte,
    b5: UByte, b6: UByte, b7: UByte, b8: UByte,
    b9: UByte, b10: UByte, b11: UByte, b12: UByte,
    b13: UByte, b14: UByte, b15: UByte, b16: UByte
) = RData(
    byteArrayOf(
        b1.toByte(),
        b2.toByte(),
        b3.toByte(),
        b4.toByte(),
        b5.toByte(),
        b6.toByte(),
        b7.toByte(),
        b8.toByte(),
        b9.toByte(),
        b10.toByte(),
        b11.toByte(),
        b12.toByte(),
        b13.toByte(),
        b14.toByte(),
        b15.toByte(),
        b16.toByte()
    ), 0, 0
)

fun RData.Companion.ipv6(address: String): RData {
    val bytes = parseIpv6ToBytes(address)
    // Вызываем целевую функцию с 16 аргументами
    return RData.Companion.ipv6(
        bytes[0], bytes[1], bytes[2], bytes[3],
        bytes[4], bytes[5], bytes[6], bytes[7],
        bytes[8], bytes[9], bytes[10], bytes[11],
        bytes[12], bytes[13], bytes[14], bytes[15]
    )
}

private fun parseIpv6ToBytes(address: String): List<UByte> {
    val trimmed = address.trim()
    require(trimmed.isNotEmpty()) { "IPv6 address cannot be empty" }

    val hasDoubleColon = trimmed.contains("::")
    val blocks = mutableListOf<Int>()

    if (!hasDoubleColon) {
        // Обычный адрес: ровно 8 блоков
        val parts = trimmed.split(':')
        require(parts.size == 8) { "IPv6 address must have exactly 8 blocks when no '::' used" }
        for (part in parts) {
            blocks.add(parseHexBlock(part))
        }
    } else {
        // Адрес с компрессией ::
        val doubleColonParts = trimmed.split("::")
        require(doubleColonParts.size == 2) { "IPv6 address can contain at most one '::'" }

        val leftPart = doubleColonParts[0]
        val rightPart = doubleColonParts[1]

        // Парсим левую часть (до ::), фильтруя пустые строки
        val leftBlocks = if (leftPart.isEmpty()) emptyList()
        else leftPart.split(':').filter { it.isNotEmpty() }.map { parseHexBlock(it) }

        // Парсим правую часть (после ::)
        val rightBlocks = if (rightPart.isEmpty()) emptyList()
        else rightPart.split(':').filter { it.isNotEmpty() }.map { parseHexBlock(it) }

        // Сколько нулевых блоков нужно вставить?
        val missing = 8 - leftBlocks.size - rightBlocks.size
        require(missing >= 0) { "Too many blocks in compressed IPv6 address: ${leftBlocks.size + rightBlocks.size} > 8" }

        blocks.addAll(leftBlocks)
        repeat(missing) { blocks.add(0) }
        blocks.addAll(rightBlocks)
    }

    require(blocks.size == 8) { "Invalid number of 16-bit blocks: ${blocks.size}" }

    // Конвертируем 8 блоков по 16 бит в 16 байт
    val result = mutableListOf<UByte>()
    for (block in blocks) {
        val high = (block shr 8) and 0xFF
        val low = block and 0xFF
        result.add(high.toUByte())
        result.add(low.toUByte())
    }
    return result
}

private fun parseHexBlock(part: String): Int {
    if (part.isEmpty()) throw IllegalArgumentException("Empty hex block")
    val value = part.toIntOrNull(16)
        ?: throw IllegalArgumentException("Invalid hexadecimal block: '$part'")
    require(value in 0..0xFFFF) { "Hex block out of range (0..FFFF): $value" }
    return value
}