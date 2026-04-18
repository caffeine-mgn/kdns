package pw.binom.dns.protocol

import kotlin.jvm.JvmInline

@JvmInline
value class DnsClass(val raw: UShort) {
    companion object {
        /**
         * Internet - используется в 99.9% случаев. Связывает домен с IP-адресами, MX, TXT и т.д.
         */
        val IN = DnsClass(1u)

        /**
         * Chaosnet (историческое, редко, иногда используется для CH TXT версии BIND)
         */
        val CH = DnsClass(3u)

        /**
         * Hesiod (распределённая база данных MIT, почти не встречается)
         */
        val HS = DnsClass(4u)

        /**
         * Мета-класс для динамических обновлений (означает "нет класса" или удаление)
         */
        val NONE = DnsClass(254u)

        /**
         * Только в вопросе - "любой класс"
         */
        val ANY = DnsClass(254u)
    }
}