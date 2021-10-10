package fr.overrride.scs.client.command.commands

import fr.overrride.scs.client.command.{CommandException, CommandExecutor, CommandUtils}
import fr.overrride.scs.client.connection.CloudClient

import java.nio.file.{Files, Path}

class UploadExecutor(client: CloudClient) extends CommandExecutor {

    private val store = client.getRootStore

    override def execute(implicit args: Array[String]): Unit = {
        checkArgs
        val targetItem = CommandUtils.argAfter("-dest")
        val source     = Path.of(args.head)

        var lastIndex = targetItem.lastIndexOf("/")
        if (lastIndex == -1) lastIndex = targetItem.length
        val itemName  = targetItem.drop(lastIndex)
        val parent    = CommandUtils.getFolder(store, itemName)
        if (Files.notExists(source))
            throw CommandException(s"source $source does not exists.")
        if (Files.isDirectory(source)) {
            parent.uploadFolder(targetItem, source)
        } else {
            parent.uploadFile(targetItem, source)
        }
        println("Uploading done !")
    }

    private def checkArgs(implicit args: Array[String]): Unit = {
        if (args.length != 3)
            throw CommandException("syntax: upload <source> -dest <destination>")
        CommandUtils.ensureArgsContains("-dest")
    }
}
