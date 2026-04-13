package pw.binom.dns.protocol.utils

object Punycode {

    private const val DASH = '-'
    private const val BASE = 36
    private const val T_MIN = 1
    private const val T_MAX = 26
    private const val SKEW = 38
    private const val DAMP = 700
    private const val INITIAL_BIAS = 72
    private const val INITIAL_N = 128

    // ========== Unicode helpers для Kotlin Common ==========

    /**
     * Преобразует строку в список 32-битных кодовых точек
     */
    private fun String.toCodePoints(): List<Int> {
        val result = mutableListOf<Int>()
        var i = 0
        while (i < length) {
            val c1 = this[i].code
            if (c1.isHighSurrogate() && i + 1 < length) {
                val c2 = this[i + 1].code
                if (c2.isLowSurrogate()) {
                    // Суррогатная пара: объединяем в одну кодовую точку
                    val codePoint = 0x10000 + ((c1 - 0xD800) shl 10) + (c2 - 0xDC00)
                    result.add(codePoint)
                    i += 2
                    continue
                }
            }
            // Обычный символ (BMP)
            result.add(c1)
            i++
        }
        return result
    }

    /**
     * Преобразует список кодовых точек в строку
     */
    private fun List<Int>.toUtf16String(): String = buildString {
        for (cp in this@toUtf16String) {
            if (cp <= 0xFFFF) {
                // BMP: один Char
                append(cp.toChar())
            } else {
                // Вне BMP: суррогатная пара
                val cpOffset = cp - 0x10000
                val high = 0xD800 or (cpOffset shr 10)
                val low = 0xDC00 or (cpOffset and 0x3FF)
                append(high.toChar())
                append(low.toChar())
            }
        }
    }

    private fun Int.isHighSurrogate(): Boolean = this in 0xD800..0xDBFF
    private fun Int.isLowSurrogate(): Boolean = this in 0xDC00..0xDFFF

    // ========== Punycode алгоритм ==========

    fun encode(input: String): String {
        val codePoints = input.toCodePoints()
        val result = StringBuilder()

        // Копируем базовые ASCII-символы (0x20–0x7F)
        for (cp in codePoints) {
            if (cp in 0x20..0x7F) {
                result.append(cp.toChar())
            }
        }

        val basicLength = result.length
        if (basicLength > 0) result.append(DASH)

        var n = INITIAL_N
        var delta = 0
        var bias = INITIAL_BIAS
        var h = basicLength

        while (h < codePoints.size) {
            val m = codePoints.filter { it >= n }.minOrNull()
                ?: throw IllegalStateException("Can't find minimum code point")

            delta += (m - n) * (h + 1)
            n = m

            for (cp in codePoints) {
                if (cp < n) {
                    delta++
                } else if (cp == n) {
                    var q = delta
                    var k = BASE
                    while (true) {
                        val t = when {
                            k <= bias -> T_MIN
                            k >= bias + T_MAX -> T_MAX
                            else -> k - bias
                        }
                        if (q < t) break
                        result.append(encodeDigit(t + (q - t) % (BASE - t)))
                        q = (q - t) / (BASE - t)
                        k += BASE
                    }
                    result.append(encodeDigit(q))
                    bias = adaptBias(delta, h + 1, h == basicLength)
                    delta = 0
                    h++
                }
            }
            delta++
            n++
        }

        return result.toString()
    }

    fun decode(input: String): String {
        val output = mutableListOf<Int>()
        var n = INITIAL_N
        var i = 0
        var bias = INITIAL_BIAS
        var inputIndex = 0

        val lastDash = input.lastIndexOf(DASH)
        if (lastDash >= 0) {
            for (j in 0 until lastDash) {
                output += input[j].code
            }
            inputIndex = lastDash + 1
        }

        while (inputIndex < input.length) {
            val oldI = i
            var w = 1
            var k = BASE
            while (true) {
                val digit = decodeDigit(input[inputIndex++])
                i += digit * w
                val t = when {
                    k <= bias -> T_MIN
                    k >= bias + T_MAX -> T_MAX
                    else -> k - bias
                }
                if (digit < t) break
                w *= BASE - t
                k += BASE
            }
            val outputSize = output.size + 1
            bias = adaptBias(i - oldI, outputSize, oldI == 0)
            n += i / outputSize
            i %= outputSize
            output.add(i++, n)
        }

        return output.toUtf16String()
    }

    private fun adaptBias(delta: Int, numPoints: Int, firstTime: Boolean): Int {
        var d = if (firstTime) delta / DAMP else delta / 2
        d += d / numPoints
        var k = 0
        while (d > ((BASE - T_MIN) * T_MAX) / 2) {
            d /= (BASE - T_MIN)
            k += BASE
        }
        return k + ((BASE - T_MIN + 1) * d) / (d + SKEW)
    }

    private fun encodeDigit(value: Int): Char = when (value) {
        in 0..25 -> (value + 'a'.code).toChar()
        in 26..35 -> (value - 26 + '0'.code).toChar()
        else -> throw IllegalArgumentException("Illegal value to encode: $value")
    }

    private fun decodeDigit(ch: Char): Int = when (ch) {
        in 'a'..'z' -> ch.code - 'a'.code
        in '0'..'9' -> ch.code - '0'.code + 26
        else -> throw IllegalArgumentException("Illegal value to decode: '$ch'")
    }
}