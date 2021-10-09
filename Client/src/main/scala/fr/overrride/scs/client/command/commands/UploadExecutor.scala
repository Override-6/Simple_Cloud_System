package fr.overrride.scs.client.command.commands

import fr.overrride.scs.client.command.{CommandException, CommandExecutor, CommandUtils}
import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.{FileStoreFile, FileStoreFolder}

import java.nio.file.{Files, Path}

class UploadExecutor(client: CloudClient) extends CommandExecutor {

    private val store = client.getRootStore

    override def execute(implicit args: Array[String]): Unit = {
        checkArgs
        val targetItem = CommandUtils.argAfter("-dest")
        val source     = Path.of(args.head)

        val lastIndex = targetItem.lastIndexOf("/")
        val parent    = CommandUtils.getFolder(store, targetItem.take(lastIndex))
        val itemName  = targetItem.drop(lastIndex)
        if (Files.notExists(source))
            throw CommandException(s"source $source does not exists.")
        println(s"Uploading file $source to server as '$targetItem'.")
        if (Files.isDirectory(source))
            parent.uploadFolder(itemName, source)
        else
            parent.uploadFile(itemName, source)
        println("Uploading done !")
    }

    private def checkArgs(implicit args: Array[String]): Unit = {
        if (args.length != 3)
            throw CommandException("syntax: upload <source> -dest <destination>")
        CommandUtils.ensureArgsContains("-dest")
    }
}
