package fr.overrride.scs.client.fs

import fr.overrride.scs.encryption.UserSecrets
import fr.overrride.scs.stream.{PacketInputStream, RemoteFileReader}

import java.io.OutputStream

class ClientSideRemoteFileReader(secrets: UserSecrets, in: PacketInputStream) extends RemoteFileReader(in) {

    override protected def writeContent(out: OutputStream, segment: Array[Byte]): Unit = {
        super.writeContent(out, secrets.decrypt(segment))
    }
}
