package fr.overrride.scs.client.connection

import fr.overrride.scs.client.fs.ClientSideFileStoreFolder
import fr.overrride.scs.common.fs.FileStoreItemInfo
import fr.overrride.scs.stream.{PacketInputStream, PacketOutputStream}

import java.net.Socket

class CloudClient(socket: Socket) {

    private var open                          = true
    private[connection] val clientThreadGroup = new ThreadGroup("Client Thread Group")
    private var store: ClientSideFileStoreFolder = _

    private lazy val pis = new PacketInputStream(socket.getInputStream)
    private lazy val pos = new PacketOutputStream(socket.getOutputStream)

    def getPacketInputStream: PacketInputStream = {
        ensureOpen()
        pis
    }

    def getPacketOutputStream: PacketOutputStream = {
        ensureOpen()
        pos
    }

    def getRootStore: ClientSideFileStoreFolder = {
        ensureOpen()
        store
    }

    def startClient(): Unit = {
        open = true
        new Thread(
            clientThreadGroup,
            () => startClient0(),
            "Server Packet Reader"
        ).start()
    }

    private def startClient0(): Unit = {
        val in = getPacketInputStream
        store = new ClientSideFileStoreFolder(FileStoreItemInfo("/", isFolder = true, -1), this)
        while (open) {
            val packet = in.readPacket()
            println(s"packet = $packet")
        }
    }

    @inline
    private def ensureOpen(): Unit = {
        if (!open)
            throw new IllegalStateException("Cloud Client is not open.")
    }

}