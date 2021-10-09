package fr.overrride.scs.client.connection

import fr.overrride.scs.stream.PacketInputStream

import java.net.Socket

class CloudClient(socket: Socket) {

    private var open                          = true
    private[connection] val clientThreadGroup = new ThreadGroup("Client Thread Group")

    def startClient(): Unit = {
        open = true
        new Thread(
            clientThreadGroup,
            () => startClient0(),
            "Server Packet Reader"
        ).start()
    }

    private def startClient0(): Unit = {
        val in = new PacketInputStream(socket.getInputStream)
        while (open) {
            val packet = in.readPacket()
            println(s"packet = $packet")
        }
    }

}
