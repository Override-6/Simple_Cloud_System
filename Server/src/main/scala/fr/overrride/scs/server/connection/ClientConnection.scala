package fr.overrride.scs.server.connection

import fr.overrride.scs.stream.PacketInputStream

import java.net.Socket

class ClientConnection(socket: Socket, server: CloudServer) {

    private val clientAddress = socket.getInetAddress
    private var open          = false

    def startReception(): Unit = {
        open = true
        new Thread(
            server.serverThreadGroup,
            () => startReception0(),
            s"$clientAddress's Packet Reader"
        ).start()
    }

    private def startReception0(): Unit = {
        val in = new PacketInputStream(socket.getInputStream)
        while (open) {
            val packet = in.readPacket()
            println(s"packet = packet")
        }
    }

}
