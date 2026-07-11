package pw.binom.dns.protocol

import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.readByteArray

/**
 * DNS Dynamic Update message (RFC 2136).
 *
 * Uses the same wire format as [DnsPackage], but with semantically
 * distinct sections:
 * - [zone] — the zone to update (1 question)
 * - [prerequisite] — records that must (not) exist (answer section)
 * - [update] — records to add or delete (authority section)
 * - [additional] — additional records (e.g. OPT)
 *
 * ```kotlin
 * val update = DnsUpdate(
 *     zone = Question("example.com", DnsType.SOA, DnsClass.IN),
 *     prerequisite = listOf(
 *         Resource("host.example.com", DnsType.A, DnsClass.NONE, 0u, ...),  // must NOT exist
 *     ),
 *     update = listOf(
 *         Resource("host.example.com", DnsType.A, DnsClass.IN, 300u, RData.ipv4("10.0.0.1")),
 *     ),
 * )
 *
 * val buffer = Buffer()
 * update.write(buffer)
 * ```
 */
data class DnsUpdate(
    val header: DnsHeader,
    val zone: Question,
    val prerequisite: List<Resource>,
    val update: List<Resource>,
    val additional: List<Resource>,
) {
    companion object {
        /**
         * Reads a Dynamic Update message from raw bytes.
         * Validates that [DnsHeader.opcode] is [Opcode.UPDATE].
         */
        fun read(data: ByteArray): DnsUpdate {
            val pkg = DnsPackage.read(data)
            require(pkg.header.opcode == Opcode.UPDATE) {
                "Expected OPCODE=UPDATE (5), got ${pkg.header.opcode.code}"
            }
            require(pkg.queries.size == 1) {
                "UPDATE must have exactly 1 zone question, got ${pkg.queries.size}"
            }
            return DnsUpdate(
                header = pkg.header,
                zone = pkg.queries.first(),
                prerequisite = pkg.answer,
                update = pkg.authority,
                additional = pkg.additional,
            )
        }
    }

    /** Writes the UPDATE message to [sink]. */
    fun write(sink: Sink) {
        val pkg = DnsPackage(
            header = header,
            queries = listOf(zone),
            answer = prerequisite,
            authority = update,
            additional = additional,
        )
        pkg.write(sink)
    }
}
