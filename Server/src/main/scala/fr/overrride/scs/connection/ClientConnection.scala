package fr.overrride.scs.connection

import java.net.Socket

class ClientConnection(socket: Socket, server: CloudServer) {

    private val clientAddress = socket.getInetAddress
    private var open          = false

    def startReception(): Unit = {
        open = true
        new Thread(server.serverThreadGroup, () => startReception0(), s"$clientAddress's packet reader")
    }

    private def startReception0(): Unit = {
        val in = socket.getInputStream
        while (open) {
            val got = in.readAllBytes()
            println(s"got = ${new String(got)}")
        }
    }

}
