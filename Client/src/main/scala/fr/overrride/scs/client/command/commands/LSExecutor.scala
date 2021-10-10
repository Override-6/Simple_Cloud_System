package fr.overrride.scs.client.command.commands

import fr.overrride.scs.client.command.{CommandExecutor, CommandUtils}
import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.{FileStoreFile, FileStoreFolder}

import java.text.SimpleDateFormat
import java.util.Date

class LSExecutor(client: CloudClient) extends CommandExecutor {

    private val store     = client.getRootStore
    private val formatter = new SimpleDateFormat("dd/MM/yy hh:mm:ss")

    override def execute(implicit args: Array[String]): Unit = {
        val dir    = args.headOption.getOrElse("/")
        val folder = CommandUtils.getFolder(store, dir)
        printFolder(folder)
    }

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
