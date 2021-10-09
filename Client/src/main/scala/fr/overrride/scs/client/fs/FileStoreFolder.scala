package fr.overrride.scs.client.fs

import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.FileStoreItemInfo
import fr.overrride.scs.common.packet.exception.UnexpectedPacketException
import fr.overrride.scs.common.packet.request._
import fr.overrride.scs.common.packet.{ObjectPacket, Packet, ValPacket}
import fr.overrride.scs.stream.{RemoteFileReader, RemoteFileWriter}

import java.nio.file.Path

class FileStoreFolder(override val info: FileStoreItemInfo,
                      client: CloudClient) extends FileStoreItem {

    private val out = client.getPacketOutputStream
    private val in  = client.getPacketInputStream

    def forEachFile(f: FileStoreItem => Unit): Unit = makeRequest(FileStoreFolderContentRequest) {
        in.readPacket() match {
            case FileStoreFolderContentResponse(folder, items) =>
                if (folder != info)
                    throw new UnexpectedPacketException(s"Received Content response for a request that targeted folder ${folder.relativePath}. Expected response for folder ${info.relativePath}.")
                items.map(infoToItem).foreach(f)

            case other => throw new UnexpectedPacketException(s"Received unexpected packet '$other', expected ${classOf[FileStoreFolderContentResponse].getSimpleName}")
        }
    }

    def isFilePresent(name: String): Boolean = {
        out.writePacket(FileStoreContentCheckRequest(info, name))
        in.readPacket() match {
            case ValPacket(bool: Boolean) => bool
            case o                        => throw new UnexpectedPacketException(s"Received unexpected response '$o'.")
        }
    }

    def downloadFile(name: String, dest: Path): Unit = makeRequest(FileUploadRequest, name) {
        new RemoteFileReader(in).readFile(dest)
    }

    def uploadFile(name: String, dest: Path, segmentSize: Int = 150000): Unit = makeRequest(FileDownloadRequest, name) {
        val remotePath = relativize(name)
        new RemoteFileWriter(out).writeFile(dest, remotePath, segmentSize)
    }

    def downloadFolder(folderName: String, dest: Path): Unit = {
        transferFolder(folderName, dest)(_.downloadFile(_, _))
    }

    def uploadFolder(folderName: String, dest: Path): Unit = {
        transferFolder(folderName, dest)(_.uploadFile(_, _))
    }

    private def transferFolder(folderName: String, dest: Path)(transferFile: (FileStoreFolder, String, Path) => Unit): Unit = {
        val remotePath = relativize(folderName)
        val subFolder  = new FileStoreFolder(FileStoreItemInfo(remotePath, isFolder = true), client)
        subFolder.forEachFile(folderItem => {
            val info     = folderItem.info
            val itemPath = info.relativePath
            val itemDest = dest.resolve(folderName)
            val itemName = itemPath.drop(itemPath.lastIndexOf('/'))
            if (info.isFolder) {
                subFolder.transferFolder(itemName, itemDest)(transferFile)
            } else {
                transferFile(subFolder, itemName, itemDest)
            }
        })
    }

    private def infoToItem(info: FileStoreItemInfo): FileStoreItem = {
        if (info.isFolder)
            new FileStoreFolder(info, client)
        else
            new FileStoreFile(info)
    }

    private def relativize(fileName: String): String = info.relativePath + "/" + fileName

    private def makeRequest(requestPacket: String => Packet, fileName: String = "")(onAccepted: => Unit): Unit = {
        val packet = requestPacket(relativize(fileName))
        out.writePacket(packet)
        in.readPacket() match {
            case ObjectPacket(possibleErrorMsg: Option[String]) =>
                possibleErrorMsg match {
                    case Some(errorMsg) => throw new FileTransferException(s"Could not download file '$fileName' from server : $errorMsg")
                    case None           => onAccepted
                }

            case other => throw new UnexpectedPacketException(s"Received unexpected packet $other, expected ObjectPacket(Option[String]).")
        }
    }

}
