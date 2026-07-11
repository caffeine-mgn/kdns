# План развития kDNS

Недостающие реализации DNS wire format.

- [x] **#1** EDNS(0) (RFC 6891) — OPT pseudo-записи, UDP buffer size, extended RCODE
  ✅ Опции OPT: DnsType.OPT, OptRecord, парсинг TTL-полей, roundtrip
- [x] **#2** DNSSEC — парсеры RRSIG, NSEC, NSEC3, DS, DNSKEY
  ✅ DsRecord, DnskeyRecord, RrsigRecord, NsecRecord, Nsec3Record + типы в DnsType
- [ ] **#3** TSIG / SIG(0) (RFC 2845) — подписанные транзакции
- [x] **#4** Dynamic Updates (RFC 2136) — парсинг/сборка update-сообщений, PREREQUISITE, NONE/ANY
  ✅ DnsUpdate: zone/prerequisite/update/additional, валидация OPCODE
- [x] **#5** Парсеры SRV, CAA, HTTPS — типы в DnsType есть, парсеров нет
  ✅ Созданы SrvRecord.kt, CaaRecord.kt, HttpsRecord.kt
