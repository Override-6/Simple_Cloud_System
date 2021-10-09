package fr.overrride.scs.client.command.commands

import fr.overrride.scs.client.command.{CommandExecutor, CommandUtils}
import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.FileStoreFolder

import java.text.SimpleDateFormat
import java.util.Date

class LSExecutor(client: CloudClient) extends CommandExecutor {

    private val store = client.getRootStore

    override def execute(implicit args: Array[String]): Unit = {
        val dir    = args.headOption.getOrElse("/")
        val folder = CommandUtils.getFolder(store, dir)
        printFolder(folder)
    }

    private def printFolder(folder: FileStoreFolder): Unit = {
        val formatter = new SimpleDateFormat("dd/MM/yy hh:mm:ss")
        val dir       = folder.info.lastModified
        val date      = formatter.format(new Date(dir))
        println(s"[$date] $dir:")
        val items = folder.getAvailableItems
        if (items.isEmpty) {
            println("<Empty>")
            return
        }
        items.foreach(item => {
            val info = item.info
            val date = formatter.format(new Date(info.lastModified))
            println(s"\t[$date] /${item.info.relativePath}" + (if (info.isFolder) "/..." else ""))
        })
    }

}
