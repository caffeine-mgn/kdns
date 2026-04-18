package pw.binom.dns.protocol

import kotlin.jvm.JvmInline

@JvmInline
value class RCode(val raw: Byte) {
    companion object {
        /**
         * No error
         */
        val NOERROR = RCode(0)

        /**
         * Format error
         */
        val FORMERR = RCode(1)

        /**
         * Server failure
         */
        val SERVFAIL = RCode(2)

        /**
         * The name does not exist
         */
        val NXDOMAIN = RCode(3)

        /**
         * The operation requested is not implemented
         */
        val NOTIMP = RCode(4)

        /**
         * The operation was refused by the server
         */
        val REFUSED = RCode(5)

        /**
         * The name exists
         */
        val YXDOMAIN = RCode(6)

        /**
         * The RRset (name, type) exists
         */
        val YXRRSET = RCode(7)

        /**
         * The RRset (name, type) does not exist
         */
        val NXRRSET = RCode(8)

        /**
         * The requestor is not authorized to perform this operation
         */
        val NOTAUTH = RCode(9)

        /**
         * The zone specified is not a zone
         */
        val NOTZONE = RCode(10)

        // ----====EDNS extended rcodes====----

        /**
         * Unsupported EDNS level
         */
        val BADVERS = RCode(16)

        // ----====SIG/TKEY only rcodes====----
        /**
         * The signature is invalid (TSIG/TKEY extended error)
         */
        val BADSIG = RCode(16)

        /**
         * The key is invalid (TSIG/TKEY extended error)
         */
        val BADKEY = RCode(17)

        /**
         * The time is out of range (TSIG/TKEY extended error)
         */
        val BADTIME = RCode(18)

        /**
         * The mode is invalid (TKEY extended error)
         */
        val BADMODE = RCode(19)

        /**
         * Duplicate key name (TKEY extended error)
         */
        val BADNAME = RCode(20)

        /**
         * Algorithm not supported (TKEY extended error)
         */
        val BADALG = RCode(21)

        /**
         * Bad truncation (RFC 4635)
         */
        val BADTRUNC = RCode(22)

        /**
         * Bad or missing server cookie (RFC 7873)
         */
        val BADCOOKIE = RCode(22)
    }
}