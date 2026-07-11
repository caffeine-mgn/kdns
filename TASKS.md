# План развития kDNS

Недостающие реализации DNS-стандартов и расширений.

- [ ] **#1** EDNS(0) (RFC 6891) — OPT pseudo-записи, UDP buffer size, extended RCODE
- [ ] **#2** DNSSEC (RFC 4033–4035) — парсеры RRSIG, NSEC, NSEC3, DS, DNSKEY
- [ ] **#3** TSIG / SIG(0) (RFC 2845) — подписанные транзакции
- [ ] **#4** Dynamic Updates (RFC 2136) — high-level API для update-сообщений, PREREQUISITE, NONE/ANY
- [ ] **#5** Парсеры SRV, CAA, HTTPS — типы есть в DnsType, парсеров нет
- [ ] **#6** Truncated (TC) responses — обработка TC=1, fallback на TCP
- [ ] **#7** Damage tolerance — устойчивость к битым пакетам (мягкая обработка вместо исключений)
