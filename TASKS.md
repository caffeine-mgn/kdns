# Проблемы и улучшения модуля `core/`

## 🔴 Критические баги

- [x] **#1** HINFORecord.from() не возвращает значение — отсутствует `return`. Парсинг HINFO полностью сломан.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/records/HINFORecord.kt:13`*
  ✅ Исправлено: добавлен возвращаемый тип `HINFORecord` и `return`

- [x] **#2** Resource.write() записывает весь `raw`-буфер как RDATA вместо сегмента. При парсинге пакета `raw` — это весь DNS-пакет.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/Resource.kt:46-47`*
  ✅ Исправлено: `rdata.raw` → `rdata.subData`

- [x] **#3** DnsClass.ANY = 254 вместо 255 (коллизия с NONE). Нарушение RFC 1035.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/DnsClass.kt:31`*
  ✅ Исправлено: ANY → DnsClass(255u)

- [x] **#4** IPV4Record.ipv4(data, offset) игнорирует параметр `offset` — читает с индекса 0. Плюс неимпортированный `data.toHexString()`.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/records/IPV4Record.kt:5-11`*
  ✅ Исправлено: `data[0]` → `data[offset]`, убран `toHexString()`

- [x] **#5** Opcode.DSO = 5 вместо 6 — коллизия с UPDATE. Нарушение RFC 8490.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/Opcode.kt:29`*
  ✅ Исправлено: DSO → Opcode(6)

## 🟠 Серьёзные проблемы

- [x] **#7** MxRecord.from() принимает `raw` (весь буфер) и читает domain name с позиции 2, игнорируя `RData.offset`. Для MX-записи не в начале буфера чтение будет с неверной позиции.
  *Файлы: `core/src/commonMain/kotlin/pw/binom/dns/protocol/records/MxRecord.kt:12-15`, `RData.kt:39`*
  ✅ Исправлено: добавлен параметр `offset`, передаётся `RData.offset`

- [x] **#8** Все фабрики RData (cname, ns, ptr, ipv4, ipv6, mx, soa, txt, hinfo) передают `size=0`. `subData` всегда пуст для сконструированных записей.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/RData.kt:11-13`*
  ✅ Исправлено: size = raw.size во всех фабриках

- [x] **#9** RData.size объявлен как `Short` (signed). Длина RDATA — беззнаковое 16-битное поле. При длинах > 32767 будет отрицательное значение.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/RData.kt:6`*
  ✅ Исправлено: `Short` → `UShort`

- [x] **#10** NumberUtils (readInt/readShort) не проверяет границы массива перед обращением по индексу — `ArrayIndexOutOfBoundsException` при неверном offset.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/utils/NumberUtils.kt:3-13`*
  ✅ Исправлено: добавлен `require` с проверкой границ

- [x] **#11** Счётчики QDCOUNT/ANCOUNT/NSCOUNT/ARCOUNT в DnsPackage.read() читаются как signed Short и используются в `0 until qdcount`. При значениях > 32767 `until` даёт неверный диапазон.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/DnsPackage.kt:29-50`*
  ✅ Исправлено: `.toUShort().toInt()` — теперь беззнаковое преобразование

- [x] **#12** normalizedRdata() имеет «мёртвый» ранний return — условие `offset==0 && size==raw.size` не срабатывало из-за `size=0`.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/utils/ResourceExtensions.kt:12-15`*
  ✅ Починено автоматически исправлениями #8 (size=raw.size) и #9 (Short→UShort)

- [x] **#13** Нет активных тестов — `MainTest.kt` полностью закомментирован.  
  *Файл: `core/src/jvmMain/kotlin/pow/binom/dns/MainTest.kt`*
  ✅ Создан `core/src/jvmTest/.../DnsProtocolTest.kt` — 42 теста

## 🟡 Умеренные / мелкие замечания

- [x] **#14** RDataExtensions.kt — пустой файл, удалён.  
  ✅ Файл удалён.

- [ ] **#15** JVM target 1.8 — Java 8 давно EOL. Рекомендуется JVM_11 или JVM_17.  
  *Файл: `core/build.gradle.kts:12`*

- [ ] **#16** Смешение kotlinx-io и ktor-io API в одном модуле. Усложняет поддержку в KMP.  
  *Файл: `core/build.gradle.kts:43-44`*

- [ ] **#17** Закомментированные зависимости (`kotlinx.coroutines.core`, `ktor.server.cio`) — стоит удалить.  
  *Файл: `core/build.gradle.kts:45-46`*

- [ ] **#18** normalizedRdata() возвращает `null` для SRV, CAA, DS, DNSKEY, HTTPS, HINFO — неожиданно для клиентов.  
  *Файл: `core/src/commonMain/kotlin/pw/binom/dns/protocol/utils/ResourceExtensions.kt:23`*
