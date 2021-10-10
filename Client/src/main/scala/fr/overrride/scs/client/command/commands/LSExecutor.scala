package fr.overrride.scs.client.command.commands

import fr.overrride.scs.client.command.{CommandExecutor, CommandUtils}
import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.{FileStoreFile, FileStoreFolder}

import java.text.SimpleDateFormat
import java.util.Date


/**
 * Executes the `ls` command.
 * @param client the client's connection
 * */
class LSExecutor(client: CloudClient) extends CommandExecutor {

    private val store     = client.getRootStore
    private val formatter = new SimpleDateFormat("dd/MM/yy hh:mm:ss")

    /**
     * Syntax: ls "folder"
     * This command will print the content of the targeted folder.
     * If no folder is set or if the given path targets a file, the command fails.
     * @param args the command line arguments
     * */
    override def execute(implicit args: Array[String]): Unit = {
        val dir    = args.headOption.getOrElse("/")
        val folder = CommandUtils.getFolder(store, dir, false)
        printFolder(folder)
    }

    /**
     *
     * @param folder the folder content to print in the console
     */
    private def printFolder(folder: FileStoreFolder): Unit = {
        val dir  = folder.info.relativePath
        val date = formatter.format(new Date(folder.info.lastModified))
        println(s"[$date] $dir:")
        val items = folder.getAvailableItems
        if (items.isEmpty) {
            println("<Empty>")
            return
        }
        items.foreach(item => {
            val info = item.info
            val date = formatter.format(new Date(info.lastModified))
            val head = s"\t[$date] ${item.info.relativePath}"
            item match {
                case _: FileStoreFolder => println(s"$head/...")
                case _: FileStoreFile   => println(head)
            }
        })
    }

}
