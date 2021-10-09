package fr.overrride.scs.client.fs

import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.FSFHelper._
import fr.overrride.scs.common.fs.PathOps.SuperPath
import fr.overrride.scs.common.fs._
import fr.overrride.scs.common.packet.exception.UnexpectedPacketException
import fr.overrride.scs.common.packet.request._
import fr.overrride.scs.common.packet.{ObjectPacket, Packet}
import fr.overrride.scs.stream.{RemoteFileReader, RemoteFileWriter}

import java.nio.file.Path

class ClientSideFileStoreFolder(client: CloudClient)(implicit override val info: FileStoreItemInfo) extends FileStoreFolder {

    private val out = client.getPacketOutputStream
    private val in  = client.getPacketInputStream

    override def getAvailableItems: Array[FileStoreItem] = {
        makeRequest(FileStoreFolderContentRequest) {
            in.readPacket() match {
                case ObjectPacket(items: Array[FileStoreItemInfo]) =>
                    items.map(infoToItem)
                case other                                         =>
                    throw new UnexpectedPacketException(s"Received unexpected packet '$other', expected ObjectPacket(Array[FileStoreItemInfo])")
            }
        }
    }

    override def findItem(name: String): Option[FileStoreItem] = {
        out.writePacket(FileStoreItemRequest(relativize(name)))
        in.readPacket() match {
            case ObjectPacket(opt: Option[FileStoreItemInfo]) => opt.map(infoToItem)
            case o                                            =>
                throw new UnexpectedPacketException(s"Received unexpected response '$o'.")
        }
    }

    override def downloadFile(name: String, dest: Path): Unit = {
        ensureFile(dest)
        makeRequest(FileUploadRequest, name) {
            new RemoteFileReader(in).readFile(dest)
        }
    }

    override def uploadFile(name: String, source: Path, segmentSize: Int): Unit = {
        ensureFile(source)
        makeRequest(FileDownloadRequest, name) {
            val remotePath = relativize(name)
            new RemoteFileWriter(out).writeFile(source, remotePath, segmentSize)
        }
    }

    override def downloadFolder(folderName: String, dest: Path): Unit = {
        ensureFolder(dest)
        transferFolder(folderName, dest)(_.downloadFile(_, _))
    }

    override def uploadFolder(folderName: String, source: Path): Unit = {
        ensureFolder(source)
        transferFolder(folderName, source)(_.uploadFile(_, _))
    }

    private def makeRequest[T](requestPacket: String => Packet, fileName: String = "")(onAccepted: => T): T = {
        val packet = requestPacket(relativize(fileName))
        out.writePacket(packet)
        in.readPacket() match {
            case ObjectPacket(possibleErrorMsg: Option[String]) =>
                possibleErrorMsg match {
                    case None           => onAccepted
                    case Some(errorMsg) => throw new FileTransferException(s"Could not download file '$fileName' from server : $errorMsg")
                }

            case other => throw new UnexpectedPacketException(s"Received unexpected packet $other, expected ObjectPacket(Option[String]).")
        }
    }

    private def infoToItem(info: FileStoreItemInfo): FileStoreItem = {
        if (info.isFolder)
            new ClientSideFileStoreFolder(client)(info)
        else
            new FileStoreFile(info)
    }

    private def transferFolder(folderName: String, path: Path)(transferFile: (FileStoreFolder, String, Path) => Unit): Unit = {
        val remotePath = relativize(folderName)
        val subFolder  = new ClientSideFileStoreFolder(client)(FileStoreItemInfo(remotePath, isFolder = true))
        subFolder.getAvailableItems.foreach(folderItem => {
            val info     = folderItem.info
            val itemPath = info.relativePath
            val itemDest = path / folderName
            val itemName = itemPath.drop(itemPath.lastIndexOf('/'))
            if (info.isFolder) {
                subFolder.transferFolder(itemName, itemDest)(transferFile)
            } else {
                transferFile(subFolder, itemName, itemDest)
            }
        })
    }

}