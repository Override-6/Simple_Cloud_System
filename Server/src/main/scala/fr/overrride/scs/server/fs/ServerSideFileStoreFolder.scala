package fr.overrride.scs.server.fs

import fr.overrride.scs.common.fs.FSFHelper._
import fr.overrride.scs.common.fs.{FileStoreFile, FileStoreFolder, FileStoreItem, FileStoreItemInfo}
import fr.overrride.scs.common.packet.NonePacket
import fr.overrride.scs.server.connection.ClientConnection
import fr.overrride.scs.stream.{RemoteFileReader, RemoteFileWriter}

import java.nio.file.{Files, Path}

class ServerSideFileStoreFolder(connection: ClientConnection, currentPath: Path)(implicit override val info: FileStoreItemInfo) extends FileStoreFolder {

    import connection.{in, out}

    override def uploadFile(name: String, source: Path, segmentSize: Int): Unit = {
        ensureFile(source)
        sendRequestAccepted()
        val writer = new RemoteFileWriter(out)
        writer.writeFile(source, relativize(name), segmentSize)
    }

    override def downloadFile(name: String, dest: Path): Unit = {
        ensureFolder(dest)
        sendRequestAccepted()
        val reader = new RemoteFileReader(in)
        reader.readFile(dest)
    }

    override def downloadFolder(folderName: String, dest: Path): Unit = {
        transferFolder(folderName, dest)(_.downloadFile(_, _))
    }

    override def uploadFolder(folderName: String, source: Path): Unit = {
        transferFolder(folderName, source)(_.uploadFile(_, _))
    }

    override def findItem(name: String): Option[FileStoreItem] = {
        val relativePath: String = relativize(name)
        val path                 = currentPath.resolve(relativePath)
        if (Files.notExists(path)) {
            return None
        }
        val subInfo = FileStoreItemInfo(relativePath, Files.isDirectory(path), Files.getLastModifiedTime(path).toMillis)
        val item    = infoToItem(subInfo)
        Some(item)
    }

    override def getAvailableItems: Array[FileStoreItem] = {
        Files.list(currentPath)
                .toArray[Path](new Array[Path](_))
                .map { path =>
                    val name         = path.getFileName.toString
                    val relativePath = relativize(name)
                    val lastModified = Files.getLastModifiedTime(path).toMillis
                    infoToItem(FileStoreItemInfo(relativePath, Files.isDirectory(path), lastModified))
                }
    }

    private def sendRequestAccepted(): Unit = {
        out.writePacket(NonePacket)
    }

    private def infoToItem(info: FileStoreItemInfo): FileStoreItem = {
        if (info.isFolder)
            new ServerSideFileStoreFolder(connection, currentPath.resolve(info.relativePath))(info)
        else
            new FileStoreFile(info)
    }

    private def transferFolder(folderName: String, path: Path)(transferFile: (FileStoreFolder, String, Path) => Unit): Unit = {
        val remotePath = relativize(folderName)
        val subFolder  = new ServerSideFileStoreFolder(connection, currentPath.resolve(folderName))(FileStoreItemInfo(remotePath, isFolder = true))
        subFolder.getAvailableItems.foreach(folderItem => {
            val info     = folderItem.info
            val itemPath = info.relativePath
            val itemDest = path.resolve(folderName)
            val itemName = itemPath.drop(itemPath.lastIndexOf('/'))
            if (info.isFolder) {
                subFolder.transferFolder(itemName, itemDest)(transferFile)
            } else {
                transferFile(subFolder, itemName, itemDest)
            }
        })
    }

}
