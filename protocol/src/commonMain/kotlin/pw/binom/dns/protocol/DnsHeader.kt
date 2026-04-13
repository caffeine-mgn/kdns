package pw.binom.dns.protocol

import kotlinx.io.Source
import pw.binom.dns.protocol.utils.readInt
import kotlin.jvm.JvmInline

@JvmInline
value class DnsHeader(val raw: Int) {
    companion object {
        const val SIZE_BYTES = Int.SIZE_BYTES
        private fun pack(
            id: Short,
            qr: Boolean,
            opcode: Opcode,   // ожидается 0..15
            aa: Boolean,
            tc: Boolean,
            rd: Boolean,
            ra: Boolean,
            z: Byte,        // ожидается 0..7
            rcode: RCode     // ожидается 0..15
        ): Int {
            // Приводим id к беззнаковому Int (0..65535)
            val idPart = (id.toInt() and 0xFFFF) shl 16

            // Собираем флаги в 16-битное значение
            var flags = 0

            // QR (1 бит) — самый старший бит флагов
            if (qr) flags = flags or (1 shl 15)

            // Opcode (4 бита, биты 11-14)
            flags = flags or ((opcode.code.toInt() and 0xF) shl 11)

            // AA (бит 10)
            if (aa) flags = flags or (1 shl 10)

            // TC (бит 9)
            if (tc) flags = flags or (1 shl 9)

            // RD (бит 8)
            if (rd) flags = flags or (1 shl 8)

            // RA (бит 7)
            if (ra) flags = flags or (1 shl 7)

            // Z (3 бита, биты 4-6)
            flags = flags or ((z.toInt() and 0x7) shl 4)

            // RCODE (4 бита, биты 0-3)
            flags = flags or (rcode.raw.toInt() and 0xF)

            // Объединяем ID и флаги
            return idPart or (flags and 0xFFFF)
        }

        fun read(data: ByteArray, offset: Int = 0): DnsHeader = DnsHeader(readInt(data, offset))
        fun read(input: Source): DnsHeader = DnsHeader(input.readInt())
    }

    constructor(
        id: Short,
        qr: Boolean,
        opcode: Opcode,   // ожидается 0..15
        aa: Boolean,
        tc: Boolean,
        rd: Boolean,
        ra: Boolean,
        z: Byte,        // ожидается 0..7
        rcode: RCode     // ожидается 0..15
    ) : this(
        pack(
            id = id,
            qr = qr,
            opcode = opcode,
            aa = aa,
            tc = tc,
            rd = rd,
            ra = ra,
            z = z,
            rcode = rcode
        )
    )

    // 16-bit identifier assigned by the program that generates the query
    val id: Short
        get() = (raw shr 16).toShort()

    private val flags: UShort
        get() = (raw and 0xFFFF).toUShort()

    /**
     * Query (false) or Response (true)
     */
    val qr: Boolean
        get() = ((flags.toInt() shr 15) and 1) == 1

    /**
     * Operation code: 0 = standard query, 1 = inverse query, 2 = status, etc.
     */
    val opcode: Opcode
        get() = Opcode(((flags.toInt() shr 11) and 0xF).toByte())

    /**
     * Authoritative Answer – set if responding server is authoritative for the domain
     */
    val aa: Boolean
        get() = ((flags.toInt() shr 10) and 1) == 1

    /**
     * Truncation – message was truncated to fit UDP datagram
     */
    val tc: Boolean
        get() = ((flags.toInt() shr 9) and 1) == 1

    /**
     * Recursion Desired – client asks server to resolve query recursively
     */
    val rd: Boolean
        get() = ((flags.toInt() shr 8) and 1) == 1

    /**
     * Recursion Available – server indicates it supports recursive resolution
     */
    val ra: Boolean
        get() = ((flags.toInt() shr 7) and 1) == 1

    /**
     * Reserved (must be zero) – 3 bits
     */
    val z: Byte
        get() = ((flags.toInt() shr 4) and 0x7).toByte()

    /**
     * Response code – 0 = no error, 1 = format error, 2 = server failure, 3 = name error, etc.
     */
    val rcode: RCode
        get() = RCode((flags.toInt() and 0xF).toByte())

    fun copy(
        id: Short = this.id,
        qr: Boolean = this.qr,
        opcode: Opcode = this.opcode,   // ожидается 0..15
        aa: Boolean = this.aa,
        tc: Boolean = this.tc,
        rd: Boolean = this.rd,
        ra: Boolean = this.ra,
        z: Byte = this.z,        // ожидается 0..7
        rcode: RCode = this.rcode,     // ожидается 0..15
    ) = DnsHeader(
        id = id,
        qr = qr,
        opcode = opcode,
        aa = aa,
        tc = tc,
        rd = rd,
        ra = ra,
        z = z,
        rcode = rcode
    )

    override fun toString() =
        "DnsHeader(id=$id, qr=$qr, opcode=$opcode, aa=$aa, tc=$tc, rd=$rd, ra=$ra, z=$z, rcode=$rcode)"
}
