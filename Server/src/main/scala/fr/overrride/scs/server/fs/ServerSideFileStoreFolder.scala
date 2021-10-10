package fr.overrride.scs.server.fs

import fr.overrride.scs.common.fs.FSFHelper._
import fr.overrride.scs.common.fs.PathOps.SuperPath
import fr.overrride.scs.common.fs.{FileStoreFile, FileStoreFolder, FileStoreItem, FileStoreItemInfo}
import fr.overrride.scs.common.packet.ObjectPacket
import fr.overrride.scs.server.connection.{ClientConnection, ServerSideRemoteFileWriter}
import fr.overrride.scs.stream.RemoteFileReader

import java.nio.file.{Files, Path}
import scala.util.{Failure, Success, Try}

class ServerSideFileStoreFolder(connection: ClientConnection, currentPath: Path)(implicit override val info: FileStoreItemInfo) extends FileStoreFolder {

    private val in            = connection.in
    private val out           = connection.out
    private val clientAddress = connection.clientAddress

    override def createFile(name: String): Unit = {
        Files.createFile(currentPath / name)
        sendRequestAccepted()
    }

    override def createFolder(name: String): Unit = {
        Files.createDirectory(currentPath / name)
        sendRequestAccepted()
    }

    override def uploadFile(name: String, source: Path): Unit = {
        Try(ensureFile(source, false)) match {
            case Failure(e) =>
                sendRequestRefused(name, e.getMessage)
            case Success(_) =>
                sendRequestAccepted()
                val relativePath = relativize(name)
                println(s"Client $clientAddress is downloading file $relativePath...")
                val writer = new ServerSideRemoteFileWriter(out)
                writer.writeFile(source, relativePath)
        }

    }

    override def downloadFile(name: String, dest: Path): Unit = {
        Try(ensureFile(dest)) match {
            case Failure(e) =>
                sendRequestRefused(name, e.getMessage)
            case Success(_) =>
                sendRequestAccepted()
                println(s"Client $clientAddress is uploading file ${currentPath / name}...")
                val reader = new RemoteFileReader(in)
                reader.readFile(dest)
        }
    }

    override def downloadFolder(folderName: String, dest: Path): Unit = {
        transferFolder(folderName, dest)(_.downloadFile(_, _))
    }

    override def uploadFolder(folderName: String, source: Path): Unit = {
        transferFolder(folderName, source)(_.uploadFile(_, _))
    }

    override def findItem(name: String): Option[FileStoreItem] = {
        val path = currentPath / name
        if (Files.notExists(path)) {
            return None
        }
        val relativePath: String = relativize(name)
        val subInfo              = FileStoreItemInfo(relativePath, Files.isDirectory(path), Files.getLastModifiedTime(path).toMillis)
        val item                 = infoToItem(subInfo)
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
        out.writePacket(ObjectPacket(None))
    }

    private def sendRequestRefused(fileName: String, msg: String): Unit = {
        Console.err.println(s"Client could not start file transfer for ${currentPath / fileName}: $msg")
        out.writePacket(ObjectPacket(Some(msg)))
    }

    private def infoToItem(info: FileStoreItemInfo): FileStoreItem = {
        if (info.isFolder)
            new ServerSideFileStoreFolder(connection, currentPath / info.relativePath)(info)
        else
            new FileStoreFile(info)
    }

    private def transferFolder(folderName: String, path: Path)(transferFile: (FileStoreFolder, String, Path) => Unit): Unit = {
        val remotePath = relativize(folderName)
        val subFolder  = new ServerSideFileStoreFolder(connection, currentPath / folderName)(FileStoreItemInfo(remotePath, isFolder = true))
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
