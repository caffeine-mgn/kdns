# kDNS

**kDNS** is a Kotlin Multiplatform library for parsing, constructing, and serializing DNS protocol messages. No external DNS resolver — just a pure DNS protocol implementation.

## Features

- **Pure DNS protocol** — parse and build DNS messages from scratch
- **Multiplatform** — JVM, JS, Wasm, Linux, macOS, iOS, tvOS, Windows (mingw)
- **Record types** — A, AAAA, CNAME, MX, SOA, NS, PTR, TXT, HINFO
- **DNS name compression** — full support for RFC 1035 pointer-based compression
- **IDN support** — Punycode encode/decode for internationalized domain names
- **Minimal dependencies** — only `kotlinx-io-core`

## Installation

### Maven Central

```kotlin
// build.gradle.kts
repositories {
    mavenCentral()
}

dependencies {
    implementation("pw.binom.dns:core:<version>")
}
```

Replace `<version>` with the [latest release](https://github.com/caffeine-mgn/kdns/releases) (e.g. `0.0.1`).

### Snapshots

Snapshots are published to Sonatype OSSRH:

```kotlin
repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("pw.binom.dns:core:1.0.0-SNAPSHOT")
}
```

## Quick Start

### Parse a DNS response from bytes

```kotlin
import pw.binom.dns.protocol.DnsPackage
import pw.binom.dns.protocol.DnsType

val responseBytes: ByteArray = // ... received from a DNS server

val pkg = DnsPackage.read(responseBytes)

println("ID: ${pkg.header.id}")
println("QR: ${pkg.header.qr}")        // false = query, true = response
println("RCODE: ${pkg.header.rcode}")  // response code

// Iterate over answer records
pkg.answer.forEach { resource ->
    when (resource.type) {
        DnsType.A -> println("A: ${resource.rdata.ipv4()}")
        DnsType.AAAA -> println("AAAA: ${resource.rdata.ipv6()}")
        DnsType.CNAME -> println("CNAME: ${resource.rdata.cname()}")
        DnsType.MX -> {
            val mx = resource.rdata.mx()
            println("MX: pref=${mx.preference} exchange=${mx.exchange}")
        }
        DnsType.SOA -> {
            val soa = resource.rdata.soa()
            println("SOA: mname=${soa.mname} serial=${soa.serial}")
        }
        DnsType.TXT -> println("TXT: ${resource.rdata.txt()}")
        else -> println("${resource.type}: raw=${resource.rdata.subData.toHexString()}")
    }
}
```

### Build a DNS query

```kotlin
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import pw.binom.dns.protocol.*

val header = DnsHeader(
    id = 12345,
    qr = false,          // query
    opcode = Opcode.QUERY,
    aa = false,
    tc = false,
    rd = true,           // recursion desired
    ra = false,
    z = 0,
    rcode = RCode.NOERROR,
)

val query = Question(
    name = "example.com",
    type = DnsType.A,
    clazz = DnsClass.IN,
)

val pkg = DnsPackage(
    header = header,
    queries = listOf(query),
    answer = emptyList(),
    authority = emptyList(),
    additional = emptyList(),
)

// Serialize to bytes
val buffer = Buffer()
pkg.write(buffer)
val bytes = buffer.readByteArray()
```

### Normalize RData

When a DNS message is parsed, `RData` keeps a reference to the original buffer for DNS name compression. Use `normalizedRdata()` to extract a standalone copy:

```kotlin
val resource: Resource = // ...
val normalized = resource.normalizedRdata()
// Now normalized.offset == 0, normalized.size == normalized.raw.size
```

## API Overview

All types live under `pw.binom.dns.protocol`.

| Type | Description |
|---|---|
| `DnsPackage` | Full DNS message (header + sections) |
| `DnsHeader` | 16-byte DNS header with flags (QR, Opcode, AA, TC, RD, RA, RCODE) |
| `Question` | A single DNS question (name + type + class) |
| `Resource` | A resource record (name + type + class + TTL + RData) |
| `RData` | Record data — keeps a view into the original buffer |
| `DnsClass` | DNS class (`IN`, `CH`, `HS`, `NONE`, `ANY`) |
| `DnsType` | DNS record type (`A`, `AAAA`, `CNAME`, `MX`, `SOA`, `NS`, `PTR`, `TXT`, `SRV`, etc.) |
| `Opcode` | DNS operation code (`QUERY`, `IQUERY`, `STATUS`, `NOTIFY`, `UPDATE`, `DSO`) |
| `RCode` | DNS response code (`NOERROR`, `NXDOMAIN`, `SERVFAIL`, `REFUSED`, etc.) |

### Record type extensions

Available as extension functions on `RData`:

```kotlin
rdata.ipv4()        // String  (e.g. "192.168.1.1")
rdata.ipv6()        // String  (e.g. "2001:db8::1")
rdata.cname()       // String
rdata.ns()          // String
rdata.ptr()         // String
rdata.mx()          // MxRecord
rdata.soa()         // SOARecord
rdata.txt()         // String
rdata.hinfo()       // HINFORecord
```

And factory functions on `RData.Companion`:

```kotlin
RData.ipv4("192.168.1.1")
RData.ipv6("2001:db8::1")
RData.cname("example.com")
RData.mx(preference = 10u, exchange = "mail.example.com")
RData.soa(SOARecord(mname = "ns1.example.com", ...))
RData.txt("v=spf1 include:_spf.example.com ~all")
```

## Supported targets

| Target | Status |
|---|---|
| JVM (1.8+) | ✅ |
| JS (Browser + Node.js) | ✅ |
| Wasm (WasmJs) | ✅ |
| Linux (x64, arm64) | ✅ |
| macOS (x64, arm64) | ✅ |
| iOS (x64, arm64, simulator) | ✅ |
| tvOS (x64, arm64, simulator) | ✅ |
| Windows (mingw x64) | ✅ |

## License

```
Copyright 2025 Anton (caffeine-mgn@gmail.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
