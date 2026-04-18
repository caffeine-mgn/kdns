package pw.binom.dns.protocol

import kotlin.jvm.JvmInline

@JvmInline
value class DnsType(val raw: UShort) {
    companion object {
        /**
         * IPv4-адрес
         */
        val A = DnsType(1u)

        /**
         * IPv6-адрес
         */
        val AAAA = DnsType(28u)

        /**
         * Каноническое имя (псевдоним)
         */
        val CNAME = DnsType(5u)

        /**
         * Почтовый обменник (Mail eXchange)
         */
        val MX = DnsType(15u)

        /**
         * Start of Authority — начало зоны
         */
        val SOA = DnsType(6u)

        /**
         * Name Server — DNS-сервер зоны
         */
        val NS = DnsType(2u)

        /**
         * Указатель (для обратного просмотра IP → имя)
         */
        val PTR = DnsType(12u)

        /**
         * Текстовая строка (SPF, DKIM, верификация)
         */
        val TXT = DnsType(16u)

        /**
         * Delegation Signer (DNSSEC)
         */
        val DS = DnsType(43u)

        /**
         * Ключ DNSSEC
         */
        val DNSKEY = DnsType(48u)

        /**
         * Сервисная запись (порт + имя сервера)
         */
        val SRV = DnsType(33u)

        /**
         * Разрешение для центров сертификации
         */
        val CAA = DnsType(257u)

        /**
         * Специальная запись для HTTPS (ускорение)
         */
        val HTTPS = DnsType(65u)

        /**
         * Только в вопросе — «любой тип» (ныне не рекомендуется)
         */
        val ANY = DnsType(255u)
    }
}