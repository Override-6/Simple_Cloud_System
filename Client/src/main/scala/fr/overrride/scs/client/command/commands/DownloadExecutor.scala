package fr.overrride.scs.client.command.commands

import fr.overrride.scs.client.command.{CommandException, CommandExecutor, CommandUtils}
import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.{CloudFile, CloudFolder}

import java.nio.file.Path

/**
 * Executes the `download` command.
 * @param client the client's connection
 * */
class DownloadExecutor(client: CloudClient) extends CommandExecutor {

    private val cloud = client.getRootStore

    /**
     * Syntax: download "source" -dest "destination"
     * This command will download a file or folder from the client's cloud space.
     * The "source" is the path in the cloud space
     * The destination is a path into the user's machine in which the decrypted data will be write
     * Note: The destination must be the same type as the source.
     *       ex: (Folder -> Folder) is okay
     *           (File -> Folder) is not okay
     * @param args the command line arguments
     */
    override def execute(implicit args: Array[String]): Unit = {
        checkArgs
        val targetItem  = args(0)
        val destination = Path.of(CommandUtils.argAfter("-dest"))

        val lastIndex = targetItem.lastIndexOf("/")
        //retrieves the parent folder of the targeted item
        val parent    = CommandUtils.getFolder(cloud, targetItem.take(lastIndex), false)
        val itemName  = targetItem.drop(lastIndex)
        val item      = parent.findItem(targetItem.drop(lastIndex)).getOrElse(throw CommandException(s"$targetItem does not exists."))
        //download the item with thanks to its parent
        item match {
            case _: CloudFile   =>
                println(s"Downloading file $itemName to server into $destination.")
                parent.downloadFile(itemName, destination)
            case _: CloudFolder =>
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
