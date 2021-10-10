package fr.overrride.scs.client.connection

import fr.overrride.scs.client.fs.ClientSideFileStoreFolder
import fr.overrride.scs.common.fs.FileStoreItemInfo
import fr.overrride.scs.encryption.UserSecrets
import fr.overrride.scs.stream.{PacketInputStream, PacketOutputStream}

import java.net.Socket

/**
 * A simple class that binds a socket and a [[UserSecrets]] object.
 * */
class CloudClient(socket: Socket, val secrets: UserSecrets) {

    private var open                             = true
    /**
     * The connection's cloud folder root.
     * */
    private var store: ClientSideFileStoreFolder = _

    private val pos = new PacketOutputStream(socket.getOutputStream)
    private val pis = new PacketInputStream(socket.getInputStream)

    /**
     * @return the packet input stream of the client
     */
    def getPacketInputStream: PacketInputStream = {
        ensureOpen()
        pis
    }
    /**
     * @return the packet output stream of the client
     */
    def getPacketOutputStream: PacketOutputStream = {
        ensureOpen()
        pos
    }

    /**
     * @return The connection's cloud folder root.
     * */
    def getRootStore: ClientSideFileStoreFolder = {
        ensureOpen()
        store
    }

    /**
     * Starts the client, initialises the [[ClientSideFileStoreFolder]]
     * */
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