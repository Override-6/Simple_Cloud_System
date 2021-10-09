package fr.overrride.scs.server.connection

import java.io.Externalizable
import java.net.ServerSocket
import java.nio.file.Path

class CloudServer(workFolder: Path, port: Int) {

    private             val serverSocket      = new ServerSocket(port)
    private[connection] val serverThreadGroup = new ThreadGroup("Server Thread Group")
    private var open                          = false

    def startServer(): Unit = {
        open = true
        new Thread(serverThreadGroup, () => startServer0(), "Socket Listener").start()
    }
    private def startServer0(): Unit = {
        while (open) {
            val accepted = serverSocket.accept()
            println(s"Accepted socket $accepted.")
            val connectionFolder = workFolder.resolve(accepted.getInetAddress.getHostAddress)
            val connection = new ClientConnection(accepted, connectionFolder, this)
            connection.startReception()
        }
    }

}
