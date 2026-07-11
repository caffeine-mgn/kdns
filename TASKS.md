# План развития kDNS

Недостающие реализации DNS-стандартов и расширений.

- [ ] **EDNS(0)** (RFC 6891) — OPT pseudo-записи, UDP buffer size, extended RCODE
- [ ] **DNSSEC** (RFC 4033–4035) — парсеры RRSIG, NSEC, NSEC3, DS, DNSKEY
- [ ] **TSIG / SIG(0)** (RFC 2845) — подписанные транзакции
- [ ] **Dynamic Updates** (RFC 2136) — high-level API для update-сообщений, PREREQUISITE, NONE/ANY
- [ ] **Парсеры SRV, CAA, HTTPS** — типы есть в DnsType, парсеров нет
- [ ] **Truncated (TC) responses** — обработка TC=1, fallback на TCP
- [ ] **Damage tolerance** — устойчивость к битым пакетам (мягкая обработка вместо исключений)
