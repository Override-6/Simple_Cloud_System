package fr.overrride.scs.server.connection

import fr.overrride.scs.common.fs.PathOps.SuperPath
import fr.overrride.scs.common.fs.{FileStoreFolder, FileStoreItemInfo}
import fr.overrride.scs.common.packet.request._
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
        socket.close()
        open = false
        closed = true
        println(s"Disconnected client $clientAddress")
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
                case NonFatal(e)        =>
                    close()
                    throw e
            }
        }
    }

    def handlePacket(packet: Packet): Unit = packet match {
        case CreateItemRequest(relativePath, isFolder)   =>
            val store = getStore(relativePath, true)
            if (isFolder) store.createFolder(extractFileName(relativePath))
            else store.createFile(extractFileName(relativePath))
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
            retrieveContentRequest(relativePath)
    }

    private def retrieveContentRequest(relativePath: String): Unit = {
        val path = storeFolderPath / relativePath
        if (Files.notExists(path)) {
            out.writePacket(ObjectPacket(Some(s"Folder $relativePath does not exists")))
            return
        }
        if (!Files.isDirectory(path))
            out.writePacket(ObjectPacket(Some(s"$relativePath is not a directory")))
        val items = getStore(relativePath, false)
                .getAvailableItems
                .map(_.info)
        out.writePacket(ObjectPacket(None)) //Say that the request is available
        out.writePacket(ObjectPacket(items))
    }

    private def extractFileName(relativePath: String): String = {
        relativePath.drop(relativePath.lastIndexOf('/'))
    }

    private def getStore(relativePath: String, stopAtParent: Boolean): FileStoreFolder = {
        val relPath      = if (stopAtParent) relativePath.take(relativePath.lastIndexOf('/')) else relativePath
        val path         = storeFolderPath / relPath
        val lastModified = if (Files.exists(path)) Files.getLastModifiedTime(path).toMillis else -1
        val info         = FileStoreItemInfo(relPath, true, lastModified)
        new ServerSideFileStoreFolder(this, path)(info)
    }
}
