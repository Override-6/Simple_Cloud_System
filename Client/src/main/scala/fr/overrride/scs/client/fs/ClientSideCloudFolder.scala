package fr.overrride.scs.client.fs

import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.CloudFolderHelper._
import fr.overrride.scs.common.fs.PathOps.AppendPath
import fr.overrride.scs.common.fs._
import fr.overrride.scs.common.packet.exception.UnexpectedPacketException
import fr.overrride.scs.common.packet.request._
import fr.overrride.scs.common.packet.{ObjectPacket, Packet}

import java.nio.file.{Files, Path}

/**
 * This class represents a distant folder in the server's storage.
 * Most of the time, this class performs requests to the server.
 * */
class ClientSideCloudFolder(client: CloudClient)(implicit override val info: CloudItemInfo) extends CloudFolder {

    //In/Out packet streams of the client
    private val out = client.getPacketOutputStream
    private val in  = client.getPacketInputStream

    /**
     * @see [[CloudFolder]] for documentation
     * */

    override def createFile(name: String): Unit = {
        makeRequest(CreateItemRequest(_, false), name) {}
    }

    override def createFolder(name: String): Unit = {
        makeRequest(CreateItemRequest(_, true), name) {}
    }

    override def getAvailableItems: Array[CloudItem] = {
        makeRequest(CloudFolderContentRequest) {
            in.readPacket() match {
                case ObjectPacket(items: Array[CloudItemInfo]) =>
                    items.map(infoToItem)
                case other                                     =>
                    throw new UnexpectedPacketException(s"Received unexpected packet '$other', expected ObjectPacket(Array[FileStoreItemInfo])")
            }
        }
    }

    override def findItem(name: String): Option[CloudItem] = {
        out.writePacket(CloudItemRequest(relativize(name)))
        in.readPacket() match {
            case ObjectPacket(opt: Option[CloudItemInfo]) => opt.map(infoToItem)
            case o                                        =>
                throw new UnexpectedPacketException(s"Received unexpected response '$o'.")
        }
    }

    override def downloadFile(name: String, dest: Path): Unit = {
        ensureFile(dest)
        println(s"Downloading file ${relativize(name)}...")
        makeRequest(FileUploadRequest, name) {
            new ClientSideRemoteFileReader(client.secrets, in).readFile(dest)
        }
    }

    override def uploadFile(name: String, source: Path): Unit = {
        ensureFile(source)
        println(s"Uploading file $source...")
        makeRequest(FileDownloadRequest, name) {
            val remotePath = relativize(name)
            new ClientSideRemoteFileWriter(client.secrets, out).writeFile(source, remotePath)
        }
    }

    override def downloadFolder(folderName: String, dest: Path): Unit = {
        ensureFolder(dest)
        val remotePath = relativize(folderName)
        println(s"Downloading folder $remotePath...")
        val subFolder = new ClientSideCloudFolder(client)(CloudItemInfo(remotePath, isFolder = true))
        subFolder.getAvailableItems.foreach(folderItem => {
            val info     = folderItem.info
            val itemPath = info.relativePath
            val itemName = itemPath.drop(itemPath.lastIndexOf('/'))
            val itemDest = dest / itemName
            if (info.isFolder) {
                subFolder.downloadFolder(itemName, itemDest)
            } else {
                subFolder.downloadFile(itemName, itemDest)
            }
        })
    }

    override def uploadFolder(folderName: String, source: Path): Unit = {
        ensureFolder(source)
        val remotePath = relativize(folderName)
        println(s"Uploading folder $source...")
        val subFolder = new ClientSideCloudFolder(client)(CloudItemInfo(remotePath, isFolder = true))
        Files.list(source)
                .forEach(path => {
                    val name = path.getFileName.toString
                    if (Files.isDirectory(path))
                        subFolder.uploadFolder(name, path)
                    else
                        subFolder.uploadFile(name, path)
                })
    }

    private def makeRequest[T](requestPacket: String => Packet, fileName: String = "")(onAccepted: => T): T = {
        val packet = requestPacket(relativize(fileName))
        out.writePacket(packet)
        in.readPacket() match {
            case ObjectPacket(possibleErrorMsg: Option[String]) =>
                possibleErrorMsg match {
                    case None           => onAccepted
                    case Some(errorMsg) => throw new FileTransferException(s"Could not perform request '$packet' from server : $errorMsg")
                }

            case other => throw new UnexpectedPacketException(s"Received unexpected packet $other, expected ObjectPacket(Option[String]).")
        }
    }

    private def infoToItem(info: CloudItemInfo): CloudItem = {
        if (info.isFolder)
            new ClientSideCloudFolder(client)(info)
        else
            new CloudFile(info)
    }

}