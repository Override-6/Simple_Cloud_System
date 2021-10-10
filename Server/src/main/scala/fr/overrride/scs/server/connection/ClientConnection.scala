package fr.overrride.scs.server.connection

import fr.overrride.scs.common.fs.PathOps.AppendPath
import fr.overrride.scs.common.fs.{CloudFolder, CloudItemInfo}
import fr.overrride.scs.common.packet.request._
import fr.overrride.scs.common.packet.{ObjectPacket, Packet}
import fr.overrride.scs.server.fs.ServerSideCloudFolder
import fr.overrride.scs.stream.{PacketInputStream, PacketOutputStream}

import java.net.{InetAddress, Socket, SocketException}
import java.nio.file.{Files, Path}
import scala.util.control.NonFatal

/**
 * Represents a distant Client's connection
 * @param socket the client's socket
 * @param cloudFolderPath the root folder of the client's cloud space
 * @param server the cloud server
 * @see CloudServer
 */
class ClientConnection(socket: Socket, cloudFolderPath: Path, server: CloudServer) extends AutoCloseable {

    val clientAddress: InetAddress = socket.getInetAddress
    private var open   = false
    private var closed = false

    private[server] val in  = new PacketInputStream(socket.getInputStream)
    private[server] val out = new PacketOutputStream(socket.getOutputStream)

    /**
     * Close and disconnects the client from the server
     */
    override def close(): Unit = {
        socket.close()
        open = false
        closed = true
        println(s"Disconnected client $clientAddress")
    }

    /**
     * Starts requests reception
     */
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

    /**
     * Handles a packet request
     * @param packet the request's packet
     */
    def handlePacket(packet: Packet): Unit = packet match {
        case CreateItemRequest(relativePath, isFolder)   =>
            val cloud = getStore(relativePath, true)
            if (isFolder) cloud.createFolder(extractFileName(relativePath))
            else cloud.createFile(extractFileName(relativePath))
        case FileDownloadRequest(relativePath)           =>
            getStore(relativePath, true)
                    .downloadFile(extractFileName(relativePath), cloudFolderPath / relativePath)
        case FileUploadRequest(relativePath) =>
            getStore(relativePath, true)
                    .uploadFile(extractFileName(relativePath), cloudFolderPath / relativePath)
        case CloudItemRequest(relativePath) =>
            val opt = getStore(relativePath, true)
                    .findItem(extractFileName(relativePath))
                    .map(_.info)
            out.writePacket(ObjectPacket(opt))
        case CloudFolderContentRequest(relativePath) =>
            retrieveContentRequest(relativePath)
    }

    /**
     * Performs the [[CloudFolderContentRequest]] request
     * @param relativePath the folder that the request targets
     */
    private def retrieveContentRequest(relativePath: String): Unit = {
        val path = cloudFolderPath / relativePath
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

    /**
     * @return the last element of a path
     * */
    private def extractFileName(relativePath: String): String = {
        relativePath.drop(relativePath.lastIndexOf('/'))
    }

    /**
     * @param relativePath the path of the wanted item
     * @param returnParent if the item's parent must be returned instead
     * @return the found item
     */
    private def getStore(relativePath: String, returnParent: Boolean): CloudFolder = {
        val relPath      = if (returnParent) relativePath.take(relativePath.lastIndexOf('/')) else relativePath
        val path         = cloudFolderPath / relPath
        val lastModified = if (Files.exists(path)) Files.getLastModifiedTime(path).toMillis else -1
        val info         = CloudItemInfo(relPath, true, lastModified)
        new ServerSideCloudFolder(this, path)(info)
    }
}
