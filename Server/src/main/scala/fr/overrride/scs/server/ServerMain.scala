package fr.overrride.scs.server

import fr.overrride.scs.server.connection.CloudServer

import java.nio.file.{Files, Path}

object ServerMain {

    /**
     * Starts the client.
     * @param args The program arguments. <br>
     *             Accepted arguments are: <br>
     *             -folder 'path' the folder in which users cloud space will be stored, default: "UserFiles/" <br>
     *             --server-port 'port' the server's port default: 48483
     *
     * */
    def main(implicit args: Array[String]): Unit = {
        val workFolder = getWorkFolder(args).toAbsolutePath
        val port = getPort(args)
        println(s"Using $workFolder as storage folder.")
        println(s"Starting server on port $port...")
        val server = new CloudServer(workFolder, port)
        server.startServer()
        println("Server successfully started")
    }

    /*
    * Utility methods
    * */

    private def getPort(implicit args: Array[String]): Int = {
        val port = getOrElse("--server-port", "48483").toInt
        if (port > 65535) {
            throw new IllegalArgumentException("-port number > 65535")
        }
        port
    }

    private def getWorkFolder(implicit args: Array[String]): Path = {
        val workFolder = Path.of(getOrElse("-folder", "UserFiles/"))
        if (Files.notExists(workFolder))
            Files.createDirectories(workFolder)
        if (!Files.isDirectory(workFolder))
            throw new InvalidConfigurationException(s"Working folder ($workFolder) is not a directory.")
        workFolder
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
