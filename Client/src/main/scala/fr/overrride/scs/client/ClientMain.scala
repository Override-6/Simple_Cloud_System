package fr.overrride.scs.client

import fr.overrride.scs.client.connection.CloudClient

import java.net.Socket
import java.nio.file.Path

object ClientMain {

    def main(implicit args: Array[String]): Unit = {
        val serverAddress = getOrElse("--server-address", "localhost")
        val serverPort    = getOrElse("--server-port", "48483").toInt
        println(s"Connecting to $serverAddress:$serverPort...")
        val socket     = new Socket(serverAddress, serverPort)
        val connection = new CloudClient(socket)
        connection.startClient()
        println("Connection successfully bound to server.")
        val store = connection.getRootStore
        store.uploadFile("CloudCapture.png", Path.of("C:\\Users\\maxim\\Desktop\\Capture.PNG"))
        println("Upload done.")
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
