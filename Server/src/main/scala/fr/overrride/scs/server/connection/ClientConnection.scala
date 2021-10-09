package fr.overrride.scs.server.connection

import fr.overrride.scs.common.fs.PathOps.SuperPath
import fr.overrride.scs.common.fs.{FileStoreFolder, FileStoreItemInfo}
import fr.overrride.scs.common.packet.request.{FileDownloadRequest, FileStoreFolderContentRequest, FileStoreItemRequest, FileUploadRequest}
import fr.overrride.scs.common.packet.{ObjectPacket, Packet}
import fr.overrride.scs.server.fs.ServerSideFileStoreFolder
import fr.overrride.scs.stream.{PacketInputStream, PacketOutputStream}

import java.net.{InetAddress, Socket, SocketException}
import java.nio.file.{Files, Path}
import scala.util.control.NonFatal

class ClientConnection(socket: Socket, storeFolderPath: Path, server: CloudServer) extends AutoCloseable {

    val clientAddress: InetAddress = socket.getInetAddress
    private var open   = false
    private var closed = false

    private[server] val in  = new PacketInputStream(socket.getInputStream)
    private[server] val out = new PacketOutputStream(socket.getOutputStream)

    override def close(): Unit = {
        println(s"Disconnecting client $clientAddress")
        socket.close()
        open = false
        closed = true
    }

    def startReception(): Unit = {
        open = true
        new Thread(
            server.serverThreadGroup,
            () => startReception0(),
            s"$clientAddress's Packet Reader"
        ).start()
    }

    private def startReception0(): Unit = {
        if (closed)
            throw new IllegalStateException("Client Connection is closed.")
        while (open) {
            try {
                val packet = in.readPacket()
                handlePacket(packet)
            } catch {
                case e: SocketException =>
                    Console.err.println(s"Socket exception for client $clientAddress: $e")
                    close()
                case NonFatal(e) =>
                    close()
                    throw e
            }
        }
    }

    def handlePacket(packet: Packet): Unit = packet match {
        case FileDownloadRequest(relativePath)           =>
            getStore(relativePath, true)
                    .downloadFile(extractFileName(relativePath), storeFolderPath / relativePath)
        case FileUploadRequest(relativePath)             =>
            getStore(relativePath, true)
                    .uploadFile(extractFileName(relativePath), storeFolderPath / relativePath)
        case FileStoreItemRequest(relativePath)          =>
            val opt = getStore(relativePath, true)
                    .findItem(extractFileName(relativePath))
                    .map(_.info)
            out.writePacket(ObjectPacket(opt))
        case FileStoreFolderContentRequest(relativePath) =>
            val items = getStore(relativePath, false)
                    .getAvailableItems
                    .map(_.info)
            out.writePacket(ObjectPacket(items))
    }

    private def extractFileName(relativePath: String): String = {
        relativePath.drop(relativePath.lastIndexOf('/'))
    }

    private def getStore(relativePath: String, stopAtParent: Boolean): FileStoreFolder = {
        val relPath      = if (stopAtParent) relativePath.drop(relativePath.lastIndexOf('/')) else relativePath
        val path         = storeFolderPath / relPath
        val lastModified = if (Files.exists(path)) Files.getLastModifiedTime(path).toMillis else -1
        val info         = FileStoreItemInfo(relativePath, true, lastModified)
        new ServerSideFileStoreFolder(this, path)(info)
    }
}
