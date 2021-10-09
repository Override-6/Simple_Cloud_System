package fr.overrride.scs.client.fs

import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.FileStoreItemInfo
import fr.overrride.scs.common.packet.exception.UnexpectedPacketException
import fr.overrride.scs.common.packet.{FileStoreContentCheckRequest, FileStoreFolderContentRequest, FileStoreFolderContentResponse, ValPacket}
import fr.overrride.scs.stream.RemoteFileReader

import java.nio.file.Path

class FileStoreFolder(override val info: FileStoreItemInfo, client: CloudClient) extends FileStoreItem {

    private val out = client.getPacketOutputStream
    private val in  = client.getPacketInputStream

    def forEachFile(f: FileStoreItem => Unit): Unit = {
        out.writePacket(FileStoreFolderContentRequest(info))
        in.readPacket() match {
            case FileStoreFolderContentResponse(folder, items) =>
                if (folder != info)
                    throw new UnexpectedPacketException(s"Received Content response for a request that targeted folder ${folder.relativePath}. Expected response for folder ${info.relativePath}.")
                items.map(infoToItem)
                        .foreach(f)
        }
    }

    def isFilePresent(name: String): Boolean = {
        out.writePacket(FileStoreContentCheckRequest(info, name))
        in.readPacket() match {
            case ValPacket(bool: Boolean) => bool
            case o                        => throw new UnexpectedPacketException(s"Received unexpected response '$o'.")
        }
    }

    def downloadFile(relativePath: String, dest: Path): Unit = {
        val reader = new RemoteFileReader(in)
        reader.readFile(dest)
    }

    def uploadFile(relativePath: String, dest: Path): Unit

    def downloadFolder(relativePath: String, dest: Path): Unit

    def uploadFolder(relativePath: String, dest: Path): Unit

    private def infoToItem(info: FileStoreItemInfo): FileStoreItem = {
        if (info.isFolder)
            new FileStoreFolder(info, client)
        else
            new FileStoreFile(info)
    }

}
