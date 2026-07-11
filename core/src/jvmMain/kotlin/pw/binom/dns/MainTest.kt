package pw.binom.dns
/*

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.buildPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.io.readByteArray
import pw.binom.dns.protocol.DnsPackage
import kotlin.io.use


fun main() {

    runBlocking {
        SelectorManager(Dispatchers.IO).use { selector ->
            val addr = aSocket(selector).udp().bind(port = 5333)
            val pack = addr.receive()
            val bytes = pack.packet.readByteArray()
            val p = DnsPackage.read(bytes)
            addr.send(
                Datagram(
                    buildPacket {
                        p.write(this)
                    },
                    InetSocketAddress("192.168.76.1", 53)
                )
            )

            val resp = DnsPackage.read(addr.receive().packet.readByteArray())

            addr.send(
                Datagram(
                    buildPacket {
                        resp.write(this)
                    },
                    pack.address,
                )
            )
        }
    }
}*/
