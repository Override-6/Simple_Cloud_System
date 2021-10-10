package fr.overrride.scs.server.connection

import fr.overrride.scs.server.connection.ServerSideRemoteFileWriter.EncryptionCoefficient
import fr.overrride.scs.stream.{PacketOutputStream, RemoteFileWriter}

import java.io.InputStream

class ServerSideRemoteFileWriter(out: PacketOutputStream) extends RemoteFileWriter(out) {

    override protected def readContent(in: InputStream, len: Int): Array[Byte] = {
        val segment = new Array[Byte]((len * EncryptionCoefficient).toInt)
        val count = in.read(segment)
        if (count == -1)
            return Array.empty
        segment
    }

}

object ServerSideRemoteFileWriter {

    private val EncryptionCoefficient: Double = 151_163D / 150_000D
}
