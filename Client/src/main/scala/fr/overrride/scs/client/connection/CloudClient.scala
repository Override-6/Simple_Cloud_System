package fr.overrride.scs.client.connection

import fr.overrride.scs.client.fs.ClientSideFileStoreFolder
import fr.overrride.scs.common.fs.FileStoreItemInfo
import fr.overrride.scs.stream.{PacketInputStream, PacketOutputStream}

import java.net.Socket

class CloudClient(socket: Socket) {

    private var open                          = true
    private[connection] val clientThreadGroup = new ThreadGroup("Client Thread Group")
    private var store: ClientSideFileStoreFolder = _

    private val pos = new PacketOutputStream(socket.getOutputStream)
    private val pis = new PacketInputStream(socket.getInputStream)

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
        store = new ClientSideFileStoreFolder(this)(FileStoreItemInfo("/", isFolder = true, -1))
    }


    @inline
    private def ensureOpen(): Unit = {
        if (!open)
            throw new IllegalStateException("Cloud Client is not open.")
    }

}