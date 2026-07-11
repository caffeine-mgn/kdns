package pw.binom.dns.protocol

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pw.binom.dns.protocol.records.HINFORecord
import pw.binom.dns.protocol.records.MxRecord
import pw.binom.dns.protocol.records.SOARecord
import pw.binom.dns.protocol.records.hinfo
import pw.binom.dns.protocol.records.ipv4
import pw.binom.dns.protocol.records.ipv6
import pw.binom.dns.protocol.records.mx
import pw.binom.dns.protocol.records.soa
import pw.binom.dns.protocol.records.txt
import pw.binom.dns.protocol.utils.DomainName
import pw.binom.dns.protocol.utils.readInt
import pw.binom.dns.protocol.utils.readShort
import pw.binom.dns.protocol.utils.normalizedRdata
import pw.binom.dns.protocol.utils.StringUtils
import pw.binom.dns.protocol.utils.Punycode

/**
 * Comprehensive tests for kDNS core module.
 */
class DnsProtocolTest {

    // ========================================================================
    // DnsClass
    // ========================================================================

    @Test
    fun `DnsClass constants have correct values`() {
        assertEquals(1u, DnsClass.IN.raw)
        assertEquals(3u, DnsClass.CH.raw)
        assertEquals(4u, DnsClass.HS.raw)
        assertEquals(254u, DnsClass.NONE.raw)
        assertEquals(255u, DnsClass.ANY.raw)
    }

    // ========================================================================
    // DnsType
    // ========================================================================

    @Test
    fun `DnsType constants have correct values`() {
        assertEquals(1u, DnsType.A.raw)
        assertEquals(28u, DnsType.AAAA.raw)
        assertEquals(5u, DnsType.CNAME.raw)
        assertEquals(15u, DnsType.MX.raw)
        assertEquals(6u, DnsType.SOA.raw)
        assertEquals(2u, DnsType.NS.raw)
        assertEquals(12u, DnsType.PTR.raw)
        assertEquals(16u, DnsType.TXT.raw)
        assertEquals(43u, DnsType.DS.raw)
        assertEquals(48u, DnsType.DNSKEY.raw)
        assertEquals(33u, DnsType.SRV.raw)
        assertEquals(257u, DnsType.CAA.raw)
        assertEquals(65u, DnsType.HTTPS.raw)
        assertEquals(255u, DnsType.ANY.raw)
    }

    // ========================================================================
    // Opcode
    // ========================================================================

    @Test
    fun `Opcode constants have correct values`() {
        assertEquals(0, Opcode.QUERY.code.toInt())
        assertEquals(1, Opcode.IQUERY.code.toInt())
        assertEquals(2, Opcode.STATUS.code.toInt())
        assertEquals(4, Opcode.NOTIFY.code.toInt())
        assertEquals(5, Opcode.UPDATE.code.toInt())
        assertEquals(6, Opcode.DSO.code.toInt())
    }

    // ========================================================================
    // RCode
    // ========================================================================

    @Test
    fun `RCode constants have correct values`() {
        assertEquals(0, RCode.NOERROR.raw.toInt())
        assertEquals(1, RCode.FORMERR.raw.toInt())
        assertEquals(2, RCode.SERVFAIL.raw.toInt())
        assertEquals(3, RCode.NXDOMAIN.raw.toInt())
        assertEquals(4, RCode.NOTIMP.raw.toInt())
        assertEquals(5, RCode.REFUSED.raw.toInt())
        assertEquals(6, RCode.YXDOMAIN.raw.toInt())
        assertEquals(7, RCode.YXRRSET.raw.toInt())
        assertEquals(8, RCode.NXRRSET.raw.toInt())
        assertEquals(9, RCode.NOTAUTH.raw.toInt())
        assertEquals(10, RCode.NOTZONE.raw.toInt())
        assertEquals(16, RCode.BADVERS.raw.toInt())
        assertEquals(16, RCode.BADSIG.raw.toInt())
        assertEquals(17, RCode.BADKEY.raw.toInt())
        assertEquals(18, RCode.BADTIME.raw.toInt())
        assertEquals(19, RCode.BADMODE.raw.toInt())
        assertEquals(20, RCode.BADNAME.raw.toInt())
        assertEquals(21, RCode.BADALG.raw.toInt())
        assertEquals(22, RCode.BADTRUNC.raw.toInt())
        assertEquals(22, RCode.BADCOOKIE.raw.toInt())
    }

    // ========================================================================
    // DnsHeader
    // ========================================================================

    @Test
    fun `DnsHeader constructor sets all fields correctly`() {
        val header = DnsHeader(
            id = 0x1234,
            qr = true,
            opcode = Opcode.QUERY,
            aa = true,
            tc = false,
            rd = true,
            ra = false,
            z = 0,
            rcode = RCode.NOERROR,
        )

        assertEquals(0x1234, header.id.toInt() and 0xFFFF)
        assertTrue(header.qr)
        assertEquals(Opcode.QUERY.code, header.opcode.code)
        assertTrue(header.aa)
        assertEquals(false, header.tc)
        assertTrue(header.rd)
        assertEquals(false, header.ra)
        assertEquals(0, header.z.toInt())
        assertEquals(RCode.NOERROR.raw, header.rcode.raw)
    }

    @Test
    fun `DnsHeader roundtrip via raw int`() {
        val original = DnsHeader(
            id = 0xABCD.toShort(),
            qr = true,
            opcode = Opcode.QUERY,
            aa = false,
            tc = true,
            rd = true,
            ra = true,
            z = 3,
            rcode = RCode.NXDOMAIN,
        )

        val restored = DnsHeader(original.raw)

        assertEquals(original.id, restored.id)
        assertEquals(original.qr, restored.qr)
        assertEquals(original.opcode.code, restored.opcode.code)
        assertEquals(original.aa, restored.aa)
        assertEquals(original.tc, restored.tc)
        assertEquals(original.rd, restored.rd)
        assertEquals(original.ra, restored.ra)
        assertEquals(original.z, restored.z)
        assertEquals(original.rcode.raw, restored.rcode.raw)
    }

    @Test
    fun `DnsHeader copy preserves and overrides fields`() {
        val header = DnsHeader(
            id = 1, qr = false, opcode = Opcode.QUERY,
            aa = false, tc = false, rd = false, ra = false,
            z = 0, rcode = RCode.NOERROR,
        )

        val copied = header.copy(id = 0xFFFF.toShort(), rd = true, rcode = RCode.SERVFAIL)

        assertEquals(0xFFFF, copied.id.toInt() and 0xFFFF)
        assertTrue(copied.rd)
        assertEquals(RCode.SERVFAIL.raw, copied.rcode.raw)

        // Unchanged fields
        assertEquals(header.qr, copied.qr)
        assertEquals(header.opcode.code, copied.opcode.code)
        assertEquals(header.z, copied.z)
    }

    @Test
    fun `DnsHeader toString contains all fields`() {
        val header = DnsHeader(
            id = 42, qr = false, opcode = Opcode.QUERY,
            aa = false, tc = false, rd = true, ra = false,
            z = 0, rcode = RCode.NOERROR,
        )
        val str = header.toString()
        assertTrue(str.contains("id=42"))
        assertTrue(str.contains("qr=false"))
        assertTrue(str.contains("rd=true"))
        assertTrue(str.contains("rcode="))
    }

    // ========================================================================
    // DnsPackage
    // ========================================================================

    @Test
    fun `DnsPackage roundtrip with A query`() {
        val header = DnsHeader(
            id = 0x1234, qr = false, opcode = Opcode.QUERY,
            aa = false, tc = false, rd = true, ra = false,
            z = 0, rcode = RCode.NOERROR,
        )
        val questions = listOf(
            Question(name = "example.com", type = DnsType.A, clazz = DnsClass.IN)
        )
        val pkg = DnsPackage(header, questions, emptyList(), emptyList(), emptyList())

        val buffer = Buffer()
        pkg.write(buffer)
        val bytes = buffer.readByteArray()

        val restored = DnsPackage.read(bytes)

        assertEquals(pkg.header.raw, restored.header.raw)
        assertEquals(pkg.queries.size, restored.queries.size)
        assertEquals("example.com", restored.queries[0].name)
        assertEquals(DnsType.A.raw, restored.queries[0].type.raw)
        assertEquals(DnsClass.IN.raw, restored.queries[0].clazz.raw)
    }

    @Test
    fun `DnsPackage roundtrip with A answer`() {
        val header = DnsHeader(
            id = 0x5678, qr = true, opcode = Opcode.QUERY,
            aa = true, tc = false, rd = true, ra = true,
            z = 0, rcode = RCode.NOERROR,
        )
        val questions = listOf(
            Question(name = "test.com", type = DnsType.A, clazz = DnsClass.IN)
        )
        val answers = listOf(
            Resource(
                name = "test.com",
                type = DnsType.A,
                clazz = DnsClass.IN,
                ttl = 300u,
                rdata = RData.ipv4("192.168.1.1"),
            )
        )
        val pkg = DnsPackage(header, questions, answers, emptyList(), emptyList())

        val buffer = Buffer()
        pkg.write(buffer)
        val bytes = buffer.readByteArray()

        val restored = DnsPackage.read(bytes)

        assertEquals(1, restored.queries.size)
        assertEquals("test.com", restored.queries[0].name)

        assertEquals(1, restored.answer.size)
        val ans = restored.answer[0]
        assertEquals("test.com", ans.name)
        assertEquals(DnsType.A.raw, ans.type.raw)
        assertEquals(300u, ans.ttl)
        assertEquals("192.168.1.1", ans.rdata.ipv4())
    }

    @Test
    fun `DnsPackage roundtrip with multiple record types`() {
        val header = DnsHeader(
            id = 99, qr = true, opcode = Opcode.QUERY,
            aa = true, tc = false, rd = true, ra = true,
            z = 0, rcode = RCode.NOERROR,
        )
        val questions = listOf(
            Question(name = "example.com", type = DnsType.A, clazz = DnsClass.IN)
        )
        val answers = listOf(
            Resource("example.com", DnsType.A, DnsClass.IN, 300u, RData.ipv4("1.2.3.4")),
            Resource("example.com", DnsType.AAAA, DnsClass.IN, 300u, RData.ipv6("2001:db8::1")),
            Resource("example.com", DnsType.CNAME, DnsClass.IN, 600u, RData.cname("real.example.com")),
        )
        val additional = listOf(
            Resource("ns1.example.com", DnsType.A, DnsClass.IN, 300u, RData.ipv4("10.0.0.1")),
        )
        val pkg = DnsPackage(header, questions, answers, emptyList(), additional)

        val buffer = Buffer()
        pkg.write(buffer)
        val bytes = buffer.readByteArray()
        val restored = DnsPackage.read(bytes)

        assertEquals(3, restored.answer.size)
        assertEquals("1.2.3.4", restored.answer[0].rdata.ipv4())
        assertEquals("2001:0db8:0000:0000:0000:0000:0000:0001", restored.answer[1].rdata.ipv6())
        assertEquals("real.example.com", restored.answer[2].rdata.cname())
        assertEquals("10.0.0.1", restored.additional[0].rdata.ipv4())
    }

    // ========================================================================
    // Question
    // ========================================================================

    @Test
    fun `Question write read roundtrip`() {
        val original = Question(name = "www.example.org", type = DnsType.MX, clazz = DnsClass.IN)

        val buffer = Buffer()
        original.write(buffer)
        val bytes = buffer.readByteArray()

        val (restored, _) = Question.read(bytes, 0)
        assertEquals(original.name, restored.name)
        assertEquals(original.type.raw, restored.type.raw)
        assertEquals(original.clazz.raw, restored.clazz.raw)
    }

    // ========================================================================
    // Resource
    // ========================================================================

    @Test
    fun `Resource A record roundtrip`() {
        val resource = Resource(
            "host.example.com", DnsType.A, DnsClass.IN, 86400u, RData.ipv4("10.0.0.5"),
        )

        val buffer = Buffer()
        resource.write(buffer)
        val bytes = buffer.readByteArray()

        val (restored, _) = Resource.read(bytes, 0)
        assertEquals("host.example.com", restored.name)
        assertEquals(DnsType.A.raw, restored.type.raw)
        assertEquals(DnsClass.IN.raw, restored.clazz.raw)
        assertEquals(86400u, restored.ttl)
        assertEquals("10.0.0.5", restored.rdata.ipv4())
    }

    @Test
    fun `Resource MX record roundtrip`() {
        val mxRecord = MxRecord(preference = 10u, exchange = "mail.example.com")
        val resource = Resource(
            "example.com", DnsType.MX, DnsClass.IN, 3600u, RData.mx(mxRecord),
        )

        val buffer = Buffer()
        resource.write(buffer)
        val bytes = buffer.readByteArray()

        val (restored, _) = Resource.read(bytes, 0)
        assertEquals(DnsType.MX.raw, restored.type.raw)

        val parsedMx = restored.rdata.mx()
        assertEquals(10u, parsedMx.preference)
        assertEquals("mail.example.com", parsedMx.exchange)
    }

    @Test
    fun `Resource SOA record roundtrip`() {
        val soaRecord = SOARecord(
            mname = "ns1.example.com",
            rname = "admin.example.com",
            serial = 20240301u,
            refresh = 3600u,
            retry = 900u,
            expire = 1209600u,
            minimum = 86400u,
        )
        val resource = Resource(
            "example.com", DnsType.SOA, DnsClass.IN, 3600u, RData.soa(soaRecord),
        )

        val buffer = Buffer()
        resource.write(buffer)
        val bytes = buffer.readByteArray()

        val (restored, _) = Resource.read(bytes, 0)
        assertEquals(DnsType.SOA.raw, restored.type.raw)

        val parsedSoa = restored.rdata.soa()
        assertEquals("ns1.example.com", parsedSoa.mname)
        assertEquals("admin.example.com", parsedSoa.rname)
        assertEquals(20240301u, parsedSoa.serial)
        assertEquals(3600u, parsedSoa.refresh)
        assertEquals(900u, parsedSoa.retry)
        assertEquals(1209600u, parsedSoa.expire)
        assertEquals(86400u, parsedSoa.minimum)
    }

    @Test
    fun `Resource TXT record roundtrip`() {
        val txtData = "v=spf1 include:_spf.example.com ~all"
        val resource = Resource(
            "example.com", DnsType.TXT, DnsClass.IN, 300u, RData.txt(txtData),
        )

        val buffer = Buffer()
        resource.write(buffer)
        val bytes = buffer.readByteArray()

        val (restored, _) = Resource.read(bytes, 0)
        assertEquals(DnsType.TXT.raw, restored.type.raw)
        assertEquals(txtData, restored.rdata.txt())
    }

    // ========================================================================
    // RData extension functions (record types)
    // ========================================================================

    @Test
    fun `RData ipv4 from bytes and string`() {
        val r1 = RData.ipv4(192u, 168u, 0u, 1u)
        assertEquals("192.168.0.1", r1.ipv4())

        val r2 = RData.ipv4("10.0.0.1")
        assertEquals("10.0.0.1", r2.ipv4())

        val raw = byteArrayOf(172.toByte(), 16, 0, 5)
        val r3 = RData.ipv4(raw, offset = 0)
        assertEquals("172.16.0.5", r3.ipv4())
    }

    @Test
    fun `RData ipv6 from bytes and string`() {
        val r1 = RData.ipv6("::1")
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", r1.ipv6())

        val r2 = RData.ipv6("2001:db8::ff")
        assertEquals("2001:0db8:0000:0000:0000:0000:0000:00ff", r2.ipv6())

        val r3 = RData.ipv6("fe80::1")
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:0001", r3.ipv6())
    }

    @Test
    fun `RData cname ns ptr`() {
        val cname = RData.cname("alias.example.com")
        assertEquals("alias.example.com", cname.cname())

        val ns = RData.ns("ns1.example.com")
        assertEquals("ns1.example.com", ns.ns())

        val ptr = RData.ptr("host.example.com")
        assertEquals("host.example.com", ptr.ptr())
    }

    @Test
    fun `RData hinfo`() {
        val hinfo = HINFORecord(cpu = "ARM64", os = "Linux")
        val rd = RData.hinfo(hinfo)

        val parsed = rd.hinfo()
        assertEquals("ARM64", parsed.cpu)
        assertEquals("Linux", parsed.os)
    }

    // ========================================================================
    // RData subData
    // ========================================================================

    @Test
    fun `RData subData returns correct slice for constructed data`() {
        val rd = RData.ipv4("10.0.0.1")
        val sub = rd.subData
        assertEquals(4, sub.size)
        assertEquals(10, sub[0].toInt() and 0xFF)
        assertEquals(0, sub[1].toInt() and 0xFF)
        assertEquals(0, sub[2].toInt() and 0xFF)
        assertEquals(1, sub[3].toInt() and 0xFF)
    }

    @Test
    fun `RData subData returns correct slice for parsed data`() {
        // Simulate RData from packet parsing: raw contains extra data before RDATA
        val raw = byteArrayOf(0, 0, 0, 0, 1, 2, 3, 4, 0, 0)
        val rd = RData(raw, offset = 3, size = 4u)
        val sub = rd.subData
        // offset=3 → raw[3..6] = [0, 1, 2, 3]
        assertEquals(4, sub.size)
        assertEquals(0, sub[0].toInt() and 0xFF)
        assertEquals(2, sub[2].toInt() and 0xFF)
        assertEquals(3, sub[3].toInt() and 0xFF)
    }

    // ========================================================================
    // ResourceExtensions.normalizedRdata
    // ========================================================================

    @Test
    fun `normalizedRdata returns self for already normalized RData`() {
        val rd = RData.ipv4("1.2.3.4")
        val resource = Resource("test", DnsType.A, DnsClass.IN, 300u, rd)
        val normalized = resource.normalizedRdata()
        assertNotNull(normalized)
        assertEquals(0, normalized.offset)
        assertEquals(4, normalized.size.toInt())
    }

    @Test
    fun `normalizedRdata normalizes A record from packet`() {
        val raw = byteArrayOf(1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0)
        val rd = RData(raw, offset = 0, size = 4u)
        val resource = Resource("test", DnsType.A, DnsClass.IN, 300u, rd)
        val normalized = resource.normalizedRdata()
        assertNotNull(normalized)
        assertEquals("1.2.3.4", normalized.ipv4())
    }

    @Test
    fun `normalizedRdata falls back to raw bytes for unsupported type`() {
        val raw = byteArrayOf(0, 0, 0, 0, 1, 2, 3, 4)
        val rd = RData(raw, offset = 4, size = 4u)
        val resource = Resource("test", DnsType.SRV, DnsClass.IN, 300u, rd)
        val normalized = resource.normalizedRdata()
        assertNotNull(normalized)
        assertEquals(0, normalized.offset)
        assertEquals(4u, normalized.size)
        assertTrue(byteArrayOf(1, 2, 3, 4).contentEquals(normalized.subData))
    }

    // ========================================================================
    // DomainName
    // ========================================================================

    @Test
    fun `DomainName write read roundtrip`() {
        val domains = listOf(
            "example.com",
            "www.example.com",
            "a.very.long.domain.name.example.org",
            "localhost",
            ".",
        )

        for (domain in domains) {
            val bytes = DomainName.write(domain)
            val (restored, _) = DomainName.readDns(bytes, 0)

            if (domain == ".") {
                assertEquals("", restored)
            } else {
                assertEquals(domain, restored)
            }
        }
    }

    @Test
    fun `DomainName handles compression pointers`() {
        // Create a DNS packet with compression to test pointer resolution
        // Name at offset 0: "example.com" (uncompressed)
        // Name at later offset: pointer to "example.com" + additional label
        val name1 = DomainName.write("example.com")
        val label = "sub".encodeToByteArray()
        val buf = Buffer()
        buf.write(name1)
        // Pointer to offset 0: 0xC000
        buf.write(byteArrayOf(0xC0.toByte(), 0x00.toByte()))
        // Append ".example.com" via pointer
        val bytes = buf.readByteArray()

        // Read first name
        val (name, pos) = DomainName.readDns(bytes, 0)
        assertEquals("example.com", name)

        // Read second name with pointer
        val (name2, _) = DomainName.readDns(bytes, name1.size)
        assertEquals("example.com", name2)
    }

    @Test
    fun `DomainName write encodes label length limit`() {
        val shortLabel = "a".repeat(63)
        val bytes = DomainName.write("$shortLabel.com")
        val (restored, _) = DomainName.readDns(bytes, 0)
        assertEquals("$shortLabel.com", restored)
    }

    @Test
    fun `DomainName readDns detects compression loop`() {
        // Create a loop: pointer at offset 0 → offset 2, pointer at offset 2 → offset 0
        val bytes = byteArrayOf(0xC0.toByte(), 0x02.toByte(), 0xC0.toByte(), 0x00.toByte())
        assertFailsWith<IllegalArgumentException> {
            DomainName.readDns(bytes, 0)
        }
    }

    // ========================================================================
    // Punycode
    // ========================================================================

    @Test
    fun `Punycode encode decode roundtrip`() {
        val cases = listOf(
            "münchen" to "mnchen-3ya",
            "日本語" to "wgv71a1191i",
            "example.com" to "example.com-",
        )

        for ((input, expectedEncoded) in cases) {
            val encoded = Punycode.encode(input)
            val decoded = Punycode.decode(encoded)
            assertEquals(input, decoded)
        }
    }

    @Test
    fun `Punycode plain ASCII stays unchanged`() {
        val ascii = "example"
        val encoded = Punycode.encode(ascii)
        assertEquals("example-", encoded)
        assertEquals(ascii, Punycode.decode(encoded))
    }

    // ========================================================================
    // StringUtils
    // ========================================================================

    @Test
    fun `StringUtils write read roundtrip`() {
        val strings = listOf(
            "hello",
            "a",
            "a".repeat(255),
        )

        for (s in strings) {
            val buffer = Buffer()
            StringUtils.write(s, buffer)
            val bytes = buffer.readByteArray()

            val (restored, _) = StringUtils.read(bytes, 0)
            assertEquals(s, restored)
        }
    }

    // ========================================================================
    // NumberUtils
    // ========================================================================

    @Test
    fun `readInt reads big-endian int`() {
        val data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        assertEquals(0x01020304, readInt(data, 0))
    }

    @Test
    fun `readInt reads int at offset`() {
        val data = byteArrayOf(0x00, 0x00, 0x01, 0x02, 0x03, 0x04)
        assertEquals(0x01020304, readInt(data, 2))
    }

    @Test
    fun `readInt rejects out of bounds`() {
        val data = byteArrayOf(0x01, 0x02, 0x03)
        assertFailsWith<IllegalArgumentException> {
            readInt(data, 0)
        }
    }

    @Test
    fun `readShort reads big-endian short`() {
        val data = byteArrayOf(0x01, 0x02)
        assertEquals(0x0102, readShort(data, 0).toInt() and 0xFFFF)
    }

    @Test
    fun `readShort reads short at offset`() {
        val data = byteArrayOf(0x00, 0x01, 0x02)
        assertEquals(0x0102, readShort(data, 1).toInt() and 0xFFFF)
    }

    @Test
    fun `readShort rejects out of bounds`() {
        val data = byteArrayOf(0x01)
        assertFailsWith<IllegalArgumentException> {
            readShort(data, 0)
        }
    }

    @Test
    fun `readShort handles unsigned 0xFFFF`() {
        val data = byteArrayOf(0xFF.toByte(), 0xFF.toByte())
        assertEquals(0xFFFF, readShort(data, 0).toInt() and 0xFFFF)
    }

    // ========================================================================
    // Edge cases
    // ========================================================================

    @Test
    fun `DnsPackage empty sections roundtrip`() {
        val header = DnsHeader(
            id = 0, qr = false, opcode = Opcode.QUERY,
            aa = false, tc = false, rd = false, ra = false,
            z = 0, rcode = RCode.NOERROR,
        )
        val pkg = DnsPackage(header, emptyList(), emptyList(), emptyList(), emptyList())

        val buffer = Buffer()
        pkg.write(buffer)
        val bytes = buffer.readByteArray()
        val restored = DnsPackage.read(bytes)

        assertEquals(0, restored.queries.size)
        assertEquals(0, restored.answer.size)
        assertEquals(0, restored.authority.size)
        assertEquals(0, restored.additional.size)
    }

    @Test
    fun `RData with offset reads correctly`() {
        // Simulate parsing where RDATA starts at index 5 of a larger buffer
        val raw = byteArrayOf(0, 0, 0, 0, 0, 10, 0, 0, 1)
        val rd = RData(raw, offset = 5, size = 4u)
        assertEquals(4, rd.subData.size)
        assertEquals(10, rd.subData[0].toInt() and 0xFF)
        assertEquals(1, rd.subData[3].toInt() and 0xFF)
    }

    @Test
    fun `Multiple Questions roundtrip`() {
        val header = DnsHeader(
            id = 1, qr = false, opcode = Opcode.QUERY,
            aa = false, tc = false, rd = true, ra = false,
            z = 0, rcode = RCode.NOERROR,
        )
        val queries = listOf(
            Question("example.com", DnsType.A, DnsClass.IN),
            Question("example.com", DnsType.AAAA, DnsClass.IN),
            Question("example.com", DnsType.MX, DnsClass.IN),
        )
        val pkg = DnsPackage(header, queries, emptyList(), emptyList(), emptyList())

        val buffer = Buffer()
        pkg.write(buffer)
        val bytes = buffer.readByteArray()
        val restored = DnsPackage.read(bytes)

        assertEquals(3, restored.queries.size)
        assertEquals(DnsType.A.raw, restored.queries[0].type.raw)
        assertEquals(DnsType.AAAA.raw, restored.queries[1].type.raw)
        assertEquals(DnsType.MX.raw, restored.queries[2].type.raw)
    }
}
