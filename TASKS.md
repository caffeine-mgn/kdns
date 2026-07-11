# План развития kDNS

Недостающие реализации DNS wire format.

- [x] **#1** EDNS(0) (RFC 6891) — OPT pseudo-записи, UDP buffer size, extended RCODE
  ✅ Опции OPT: DnsType.OPT, OptRecord, парсинг TTL-полей, roundtrip
- [ ] **#2** DNSSEC — парсеры RRSIG, NSEC, NSEC3, DS, DNSKEY
- [ ] **#3** TSIG / SIG(0) (RFC 2845) — подписанные транзакции
- [ ] **#4** Dynamic Updates (RFC 2136) — парсинг/сборка update-сообщений, PREREQUISITE, NONE/ANY
- [x] **#5** Парсеры SRV, CAA, HTTPS — типы в DnsType есть, парсеров нет
  ✅ Созданы SrvRecord.kt, CaaRecord.kt, HttpsRecord.kt
