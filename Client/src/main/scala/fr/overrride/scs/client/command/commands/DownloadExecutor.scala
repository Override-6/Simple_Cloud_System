package fr.overrride.scs.client.command.commands

import fr.overrride.scs.client.command.{CommandException, CommandExecutor, CommandUtils}
import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.{FileStoreFile, FileStoreFolder}

import java.nio.file.Path

class DownloadExecutor(client: CloudClient) extends CommandExecutor {

    private val store = client.getRootStore

    override def execute(implicit args: Array[String]): Unit = {
        checkArgs
        val targetItem  = args(0)
        val destination = Path.of(CommandUtils.argAfter("-dest"))

        val lastIndex = targetItem.lastIndexOf("/")
        val parent    = CommandUtils.getFolder(store, targetItem.take(lastIndex))
        val itemName  = targetItem.drop(lastIndex)
        val item      = parent.findItem(targetItem.drop(lastIndex)).getOrElse(throw CommandException(s"$targetItem does not exists."))
        item match {
            case _: FileStoreFile   =>
                println(s"Downloading file $itemName to server into $destination.")
                parent.downloadFile(itemName, destination)
            case _: FileStoreFolder =>
                parent.downloadFolder(itemName, destination)
        }
        println("Download done !")
    }

    private def checkArgs(implicit args: Array[String]): Unit = {
        if (args.length != 3)
            throw CommandException("syntax: download <source> -dest <destination>")
        CommandUtils.ensureArgsContains("-dest")
    }
}
