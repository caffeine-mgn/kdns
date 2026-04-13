package pw.binom.dns.protocol.utils

import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.charsets.decode
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.toByteArray

internal object DomainName {
    /**
     * Читает QNAME из DNS-пакета (ByteArray) с поддержкой сжатия.
     * @param data массив байт всего DNS-пакета
     * @param offset позиция, с которой начинается QNAME
     * @return Pair<domainName, nextOffset> где nextOffset – позиция после прочитанного QNAME
     * @throws IllegalArgumentException при обнаружении цикла или некорректного указателя
     */
    fun readDns(data: ByteArray, offset: Int): Pair<String, Int> {
        val visited = mutableSetOf<Int>()
        return readLabelsAt(data, offset, visited)
    }

    private fun readLabelsAt(data: ByteArray, offset: Int, visited: MutableSet<Int>): Pair<String, Int> {
        if (offset in visited) throw IllegalArgumentException("DNS compression loop at $offset")
        visited.add(offset)

        var pos = offset
        val labels = mutableListOf<String>()

        while (true) {
            val b = data[pos].toInt() and 0xFF
            when {
                b == 0 -> { // конец имени
                    pos++
                    break
                }

                b and 0xC0 == 0xC0 -> { // указатель (сжатие)
                    if (pos + 1 >= data.size) throw IllegalArgumentException("Truncated pointer")
                    val b2 = data[pos + 1].toInt() and 0xFF
                    val pointerOffset = ((b and 0x3F) shl 8) or b2
                    val (pointedName, _) = readLabelsAt(data, pointerOffset, visited)
                    labels.add(pointedName)
                    pos += 2
                    break // указатель всегда последний элемент имени
                }

                else -> { // обычная метка
                    val len = b
                    if (len == 0) {
                        pos++
                        break
                    }
                    if (pos + len >= data.size) throw IllegalArgumentException("Truncated label")
                    pos++
                    val rawBytes = data.copyOfRange(pos, pos + len)
                    val rawLabel = Charsets.ISO_8859_1.newDecoder().decode(buildPacket {
                        write(rawBytes)
                    })
                    val label = if (rawLabel.startsWith("xn--", ignoreCase = true))
                        Punycode.decode(rawLabel.substring(4))
                    else
                        rawLabel
                    labels.add(label)
                    pos += len
                }
            }
        }
        return labels.joinToString(".") to pos
    }

    fun write(domain: String): ByteArray {
        var name = domain
        if (name == ".") return byteArrayOf(0)
        if (name.endsWith('.')) name = name.substring(0, name.length - 1)

        val labels = name.split('.')
        val result = mutableListOf<Byte>()

        for (label in labels) {
            if (label.isEmpty()) continue
            // Преобразуем в ASCII (Punycode, если есть не-ASCII)
            val asciiLabel = if (label.all { it.code < 128 }) label else "xn--${Punycode.encode(label)}"
            val bytes = asciiLabel.toByteArray(Charsets.ISO_8859_1)
            require(bytes.size in 1..63) { "Label '${label}' is ${bytes.size} bytes (max 63)" }
            result.add(bytes.size.toByte())
            result.addAll(bytes.toList())
        }
        result.add(0)
        return result.toByteArray()
    }
}