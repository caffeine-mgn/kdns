# kDNS

**kDNS** — это Kotlin Multiplatform библиотека для разбора, создания и сериализации DNS-сообщений. Не является DNS-резолвером и не содержит сетевого слоя — только чистый wire format протокола.

## Возможности

- **Чистый протокол DNS** — парсинг и сборка DNS-сообщений «с нуля»
- **Мультиплатформа** — JVM, JS, Wasm, Linux, macOS, iOS, tvOS, Windows (mingw)
- **Типы записей** — A, AAAA, CNAME, MX, SOA, NS, PTR, TXT, HINFO, SRV, CAA, HTTPS
- **DNSSEC** — парсеры DS, DNSKEY, RRSIG, NSEC, NSEC3
- **EDNS(0)** — OPT pseudo-запись с UDP payload size, DO bit, extended RCODE
- **Dynamic Updates** (RFC 2136) — типизированный API для обновления зон с предусловиями
- **Сжатие имён** — полная поддержка компрессии на основе указателей (RFC 1035)
- **IDN** — кодирование/декодирование Punycode для интернациональных доменов
- **Минимум зависимостей** — только `kotlinx-io-core`

## Подключение

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

Замените `<version>` на актуальную версию из [релизов](https://github.com/caffeine-mgn/kdns/releases) (например, `0.0.1`).

### Snapshots

Снапшоты публикуются в Sonatype OSSRH:

```kotlin
repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("pw.binom.dns:core:1.0.0-SNAPSHOT")
}
```

## Быстрый старт

### Разобрать DNS-ответ из байтов

```kotlin
import pw.binom.dns.protocol.DnsPackage
import pw.binom.dns.protocol.DnsType

val responseBytes: ByteArray = // ... получено от DNS-сервера

val pkg = DnsPackage.read(responseBytes)

println("ID: ${pkg.header.id}")
println("QR: ${pkg.header.qr}")        // false = запрос, true = ответ
println("RCODE: ${pkg.header.rcode}")   // код ответа

// Обход ответных записей
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
        else -> println("${resource.type}: сырые данные=${resource.rdata.subData.toHexString()}")
    }
}
```

### Собрать DNS-запрос

```kotlin
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import pw.binom.dns.protocol.*

val header = DnsHeader(
    id = 12345,
    qr = false,          // запрос
    opcode = Opcode.QUERY,
    aa = false,
    tc = false,
    rd = true,           // рекурсия желательна
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

// Сериализация в байты
val buffer = Buffer()
pkg.write(buffer)
val bytes = buffer.readByteArray()
```

### Нормализация RData

При парсинге DNS-сообщения `RData` хранит ссылку на исходный буфер (для компрессии имён). Используйте `normalizedRdata()` чтобы получить независимую копию:

```kotlin
val resource: Resource = // ...
val normalized = resource.normalizedRdata()
// Теперь normalized.offset == 0, normalized.size == normalized.raw.size
```

## Обзор API

Все типы находятся в пакете `pw.binom.dns.protocol`.

| Тип | Описание |
|---|---|
| `DnsPackage` | Полное DNS-сообщение (заголовок + секции) |
| `DnsHeader` | 16-байтовый DNS-заголовок с флагами (QR, Opcode, AA, TC, RD, RA, RCODE) |
| `Question` | Один DNS-вопрос (имя + тип + класс) |
| `Resource` | Ресурсная запись (имя + тип + класс + TTL + RData) |
| `RData` | Данные записи — представление (view) исходного буфера |
| `DnsClass` | Класс DNS (`IN`, `CH`, `HS`, `NONE`, `ANY`) |
| `DnsType` | Тип записи (`A`, `AAAA`, `CNAME`, `MX`, `SOA`, `NS`, `PTR`, `TXT`, `SRV`, `CAA`, `HTTPS`, `DS`, `DNSKEY`, `RRSIG`, `NSEC`, `NSEC3`, `OPT`, `ANY`, и др.) |
| `Opcode` | Код операции (`QUERY`, `IQUERY`, `STATUS`, `NOTIFY`, `UPDATE`, `DSO`) |
| `RCode` | Код ответа (`NOERROR`, `NXDOMAIN`, `SERVFAIL`, `REFUSED`, и др.) |

### Расширения для типов записей

Доступны как функции-расширения на `RData`:

```kotlin
rdata.ipv4()        // String  (например "192.168.1.1")
rdata.ipv6()        // String  (например "2001:db8::1")
rdata.cname()       // String
rdata.ns()          // String
rdata.ptr()         // String
rdata.mx()          // MxRecord
rdata.soa()         // SOARecord
rdata.txt()         // String
rdata.hinfo()       // HINFORecord
rdata.srv()         // SrvRecord
rdata.caa()         // CaaRecord
rdata.https()       // HttpsRecord
rdata.ds()          // DsRecord
rdata.dnskey()      // DnskeyRecord
rdata.rrsig()       // RrsigRecord
rdata.nsec()        // NsecRecord
rdata.nsec3()       // Nsec3Record
```

И фабричные функции на `RData.Companion`:

```kotlin
RData.ipv4("192.168.1.1")
RData.ipv6("2001:db8::1")
RData.cname("example.com")
RData.mx(preference = 10u, exchange = "mail.example.com")
RData.soa(SOARecord(mname = "ns1.example.com", ...))
RData.txt("v=spf1 include:_spf.example.com ~all")
RData.srv(priority = 10u, weight = 5u, port = 443u, target = "srv.example.com")
RData.caa(flags = 0u, tag = "issue", value = "letsencrypt.org")
RData.https(priority = 1u, target = "www.example.com")
RData.ds(DsRecord(...))
RData.dnskey(DnskeyRecord(...))
RData.rrsig(RrsigRecord(...))
```

### EDNS(0) / OPT

OPT pseudo-записи находятся в дополнительной секции. Используйте `OptRecord.from(resource.clazz, resource.ttl, resource.rdata)`:

```kotlin
val opt = pkg.additional
    .find { it.type == DnsType.OPT }
    ?.let { OptRecord.from(it.clazz.raw, it.ttl, it.rdata) }

println("UDP payload size: ${opt?.udpPayloadSize}")
println("DO bit: ${opt?.doBit}")
```

### Dynamic Updates (RFC 2136)

`DnsUpdate` — семантическая обёртка над стандартным DNS-сообщением для обновления зон:

```kotlin
val update = DnsUpdate(
    header = DnsHeader(id = 42, qr = false, opcode = Opcode.UPDATE, ...),
    zone = Question("example.com", DnsType.SOA, DnsClass.IN),
    prerequisite = listOf(
        // Утверждаем, что host.example.com AAAA НЕ существует
        Resource("host.example.com", DnsType.AAAA, DnsClass.NONE, 0u, ...),
    ),
    update = listOf(
        // Добавляем A запись
        Resource("host.example.com", DnsType.A, DnsClass.IN, 300u, RData.ipv4("10.0.0.1")),
    ),
)

val buffer = Buffer()
update.write(buffer)
socket.send(buffer.readByteArray())
```

### TSIG / SIG(0)

Не реализовано. TSIG требует HMAC-криптографии, которой нет в Kotlin/Common stdlib без внешних библиотек.

## Поддерживаемые платформы

| Платформа | Статус |
|---|---|
| JVM (1.8+) | ✅ |
| JS (Browser + Node.js) | ✅ |
| Wasm (WasmJs, WasmWasi) | ✅ |
| Linux (x64, arm64) | ✅ |
| macOS (x64, arm64) | ✅ |
| iOS (x64, arm64, simulator) | ✅ |
| tvOS (x64, arm64, simulator) | ✅ |
| watchOS (x64, arm32, arm64, simulator) | ✅ |
| Windows (mingw x64) | ✅ |

## Лицензия

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
