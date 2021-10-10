package fr.overrride.scs.client

import fr.overrride.scs.client.command.CommandManager
import fr.overrride.scs.client.command.commands.{DownloadExecutor, LSExecutor, UploadExecutor}
import fr.overrride.scs.client.connection.CloudClient
import fr.overrride.scs.encryption.{UserOrganization, UserSecrets, UserSecretsFactory}

import java.net.Socket
import java.nio.file.{Files, Path}
import scala.io.StdIn

object ClientMain {

    /**
     * Starts the client.
     * @param args: The program arguments.  <br>
     *      Accepted arguments are:  <br>
     *      --server-address  'host'     default: localhost <br>
     *      --server-port     'port'     default: 48483<br>
     *      --secrets-folder  'path'     default: None (the program shutdowns with a message error)
     *      --password        'password' default: Asks in console
     * */
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

    /**
     * retrieves the User's secrets
     * @param args the programs arguments
     * @see [[UserSecrets]]
     * */
    private def getSecrets(implicit args: Array[String]): UserSecrets = {
        val secretsFolder = getOrElse("--secrets-folder", null)
        if (secretsFolder == null) {
            Console.err.println("Error: argument --secrets-folder not set")
            Console.err.println("please specify a folder in which keycloud and certificates can be cloudd.")
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
        val password     = getOrElse("--password", askPassword())
        println("Creating User Secrets, This can take a while...")
        UserSecretsFactory.create(path, password, organisation)
    }

    /**
     * Asks the password to the user.
     * */
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

    /**
     * Registers all the commands in the [[CommandManager]].
     * @see [[CommandManager]]
     * @see [[LSExecutor]]
     * @see [[DownloadExecutor]]
     * @see [[UploadExecutor]]
     * */
    private def registerCommands(manager: CommandManager, connection: CloudClient): Unit = {
        manager.register("ls", new LSExecutor(connection))
        manager.register("download", new DownloadExecutor(connection))
        manager.register("upload", new UploadExecutor(connection))
    }

    /**
     * Utility method that returns the arguments that follows the 'key' arg.
     * @param   key the key argument
     * @param   defaultValue the value to return if the key argument was not found.
     * @param   args the string array to scan
     * @return the value in the given string array that follows the key, or, if no key is found, returns the default value.
     * */
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