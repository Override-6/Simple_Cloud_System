package fr.overrride.scs.server.fs

import fr.overrride.scs.common.fs.CloudFolderHelper._
import fr.overrride.scs.common.fs.PathOps.AppendPath
import fr.overrride.scs.common.fs.{CloudFile, CloudFolder, CloudItem, CloudItemInfo}
import fr.overrride.scs.common.packet.ObjectPacket
import fr.overrride.scs.server.connection.{ClientConnection, ServerSideRemoteFileWriter}
import fr.overrride.scs.stream.RemoteFileReader

import java.nio.file.{Files, Path}
import scala.util.{Failure, Success, Try}

/**
 * Represents the folder of a client's cloud space.
 * A [[ServerSideCloudFolder]] only answers to requests and perform actions on the file system
 * */
class ServerSideCloudFolder(connection: ClientConnection, currentPath: Path)(implicit override val info: CloudItemInfo) extends CloudFolder {

    private val in            = connection.in
    private val out           = connection.out
    private val clientAddress = connection.clientAddress

    /**
     * @see [[CloudFolder]] for documentation
     * */

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

    override def findItem(name: String): Option[CloudItem] = {
        val path = currentPath / name
        if (Files.notExists(path)) {
            return None
        }
        val relativePath: String = relativize(name)
        val subInfo              = CloudItemInfo(relativePath, Files.isDirectory(path), Files.getLastModifiedTime(path).toMillis)
        val item                 = infoToItem(subInfo)
        Some(item)
    }

    override def getAvailableItems: Array[CloudItem] = {
        Files.list(currentPath)
                .toArray[Path](new Array[Path](_))
                .map { path =>
                    val name         = path.getFileName.toString
                    val relativePath = relativize(name)
                    val lastModified = Files.getLastModifiedTime(path).toMillis
                    infoToItem(CloudItemInfo(relativePath, Files.isDirectory(path), lastModified))
                }
    }

    private def sendRequestAccepted(): Unit = {
        out.writePacket(ObjectPacket(None))
    }

    private def sendRequestRefused(fileName: String, msg: String): Unit = {
        Console.err.println(s"Client could not start file transfer for ${currentPath / fileName}: $msg")
        out.writePacket(ObjectPacket(Some(msg)))
    }

    private def infoToItem(info: CloudItemInfo): CloudItem = {
        if (info.isFolder)
            new ServerSideCloudFolder(connection, currentPath / info.relativePath)(info)
        else
            new CloudFile(info)
    }

    private def transferFolder(folderName: String, path: Path)(transferFile: (CloudFolder, String, Path) => Unit): Unit = {
        val remotePath = relativize(folderName)
        val subFolder  = new ServerSideCloudFolder(connection, currentPath / folderName)(CloudItemInfo(remotePath, isFolder = true))
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
