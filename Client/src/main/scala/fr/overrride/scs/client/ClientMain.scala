package fr.overrride.scs.client

import fr.overrride.scs.client.command.CommandManager
import fr.overrride.scs.client.command.commands.{DownloadExecutor, LSExecutor, UploadExecutor}
import fr.overrride.scs.client.connection.CloudClient

import java.net.Socket

object ClientMain {

    def main(implicit args: Array[String]): Unit = {
        val serverAddress = getOrElse("--server-address", "localhost")
        val serverPort    = getOrElse("--server-port", "48483").toInt
        println(s"Connecting to $serverAddress:$serverPort...")
        val socket     = new Socket(serverAddress, serverPort)
        val connection = new CloudClient(socket)
        connection.startClient()
        println("Connection successfully bound to server.")
        val manager = new CommandManager
        registerCommands(manager, connection)
        manager.start()
    }

    private def registerCommands(manager: CommandManager, connection: CloudClient): Unit = {
        manager.register("ls", new LSExecutor(connection))
        manager.register("download", new DownloadExecutor(connection))
        manager.register("upload", new UploadExecutor(connection))
    }

    //noinspection SameParameterValue
    private def getOrElse(key: String, defaultValue: String)(implicit args: Array[String]): String = {
        val index = args.indexOf(key)
        if (index < 0 || index + 1 > args.length - 1) {
            defaultValue
        } else {
            args(index + 1)
        }
    }

}
