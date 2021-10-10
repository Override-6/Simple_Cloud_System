package fr.overrride.scs.client.command.commands

import fr.overrride.scs.client.command.{CommandException, CommandExecutor, CommandUtils}
import fr.overrride.scs.client.connection.CloudClient

import java.nio.file.{Files, Path}

/**
 * executes the `upload` command.
 * @param client the client's connection
 */
class UploadExecutor(client: CloudClient) extends CommandExecutor {

    private val cloud = client.getRootStore

    /**
     * Syntax: upload "source" -dest "destination"
     * This command will upload a file or folder into the client's cloud space.
     * The "source" is the path that we want to send
     * The destination is a path into the cloud space in which the encrypted data will be send
     * Note: The destination must be the same type as the source.
     *       ex: (Folder -> Folder) is okay
     *           (File -> Folder) is not okay
     * @param args the command line arguments
     */
    override def execute(implicit args: Array[String]): Unit = {
        checkArgs
        val targetItem = CommandUtils.argAfter("-dest")
        val source     = Path.of(args.head)

        var lastIndex = targetItem.lastIndexOf("/")
        if (lastIndex == -1) lastIndex = targetItem.length
        val itemName  = targetItem.drop(lastIndex)
        val parent    = CommandUtils.getFolder(cloud, itemName, true)
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
