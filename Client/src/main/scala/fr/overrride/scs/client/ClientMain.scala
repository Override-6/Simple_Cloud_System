package fr.overrride.scs.client

import fr.overrride.scs.client.command.CommandManager
import fr.overrride.scs.client.command.commands.{DownloadExecutor, LSExecutor, UploadExecutor}
import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.common.fs.PathOps.SuperPath
import fr.overrride.scs.encryption.{UserOrganization, UserSecrets, UserSecretsFactory}

import java.net.Socket
import java.nio.file.{Files, Path}
import scala.io.StdIn

object ClientMain {

    def main(implicit args: Array[String]): Unit = {
        val serverAddress = getOrElse("--server-address", "localhost")
        val serverPort    = getOrElse("--server-port", "48483").toInt
        val secrets       = getSecrets(args)
        if (secrets == null)
            return
        println(s"Connecting to $serverAddress:$serverPort...")
        val socket     = new Socket(serverAddress, serverPort)
        val connection = new CloudClient(socket, secrets)
        connection.startClient()
        println("Connection successfully bound to server.")
        val manager = new CommandManager
        registerCommands(manager, connection)
        manager.start()
    }

    private def getSecrets(implicit args: Array[String]): UserSecrets = {
        val secretsFolder = getOrElse("--secrets-folder", null)
        if (secretsFolder == null) {
            Console.err.println("Error: argument --secrets-folder not set")
            Console.err.println("please specify a folder in which keystore and certificates can be stored.")
            return null
        }
        val path = Path.of(secretsFolder)
        if (Files.notExists(path))
            Files.createDirectories(path)
        if (!Files.isDirectory(path)) {
            Console.err.println("Error: provided secrets folder is not a folder.")
            Console.err.println(s"Folder: $path")
            return null
        }
        val organisation = UserOrganization.fromYaml(getClass.getResourceAsStream("/organisation.yaml"))
        val password = getOrElse("--password", askPassword())
        println("Creating User Secrets, This can take a while...")
        UserSecretsFactory.create(path, password, organisation)
    }

    private def askPassword(): String = {
        var password: String = null
        do {
            if (password != null)
                println("Password must be at least 6 characters.")
            print("User password : ")
            val console = System.console()
            password = if (console == null) StdIn.readLine()
            else new String(console.readPassword())
        } while (password.length < 6)
        password
    }

    private def registerCommands(manager: CommandManager, connection: CloudClient): Unit = {
        manager.register("ls", new LSExecutor(connection))
        manager.register("download", new DownloadExecutor(connection))
        manager.register("upload", new UploadExecutor(connection))
    }

    //noinspection SameParameterValue
    private def getOrElse(key: String, defaultValue: => String)(implicit args: Array[String]): String = {
        val index = args.indexOf(key)
        if (index < 0 || index + 1 > args.length - 1) {
            defaultValue
        } else {
            args(index + 1)
        }
    }

}
