# kDNS

**kDNS** — это Kotlin Multiplatform библиотека для разбора, создания и сериализации DNS-сообщений. Не является DNS-резолвером — чистая реализация протокола DNS.

## Возможности

- **Чистый протокол DNS** — парсинг и сборка DNS-сообщений «с нуля»
- **Мультиплатформа** — JVM, JS, Wasm, Linux, macOS, iOS, tvOS, Windows (mingw)
- **Типы записей** — A, AAAA, CNAME, MX, SOA, NS, PTR, TXT, HINFO
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
| `DnsType` | Тип записи (`A`, `AAAA`, `CNAME`, `MX`, `SOA`, `NS`, `PTR`, `TXT`, `SRV`, и др.) |
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
```

И фабричные функции на `RData.Companion`:

```kotlin
RData.ipv4("192.168.1.1")
RData.ipv6("2001:db8::1")
RData.cname("example.com")
RData.mx(preference = 10u, exchange = "mail.example.com")
RData.soa(SOARecord(mname = "ns1.example.com", ...))
RData.txt("v=spf1 include:_spf.example.com ~all")
```

## Поддерживаемые платформы

| Платформа | Статус |
|---|---|
| JVM (1.8+) | ✅ |
| JS (Browser + Node.js) | ✅ |
| Wasm (WasmJs) | ✅ |
| Linux (x64, arm64) | ✅ |
| macOS (x64, arm64) | ✅ |
| iOS (x64, arm64, simulator) | ✅ |
| tvOS (x64, arm64, simulator) | ✅ |
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
