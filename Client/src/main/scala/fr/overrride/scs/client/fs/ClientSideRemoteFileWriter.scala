package fr.overrride.scs.client.fs

import fr.overrride.scs.encryption.UserSecrets
import fr.overrride.scs.stream.{PacketOutputStream, RemoteFileWriter}

import java.io.InputStream

class ClientSideRemoteFileWriter(secrets: UserSecrets, out: PacketOutputStream) extends RemoteFileWriter(out) {

    /**
     * Decrypts a content using the given [[UserSecrets]]
     * */
    override protected def readContent(in: InputStream, len: Int): Array[Byte] = {
        val seg = super.readContent(in, len)
        if (seg.isEmpty)
            return seg //do not encrypt empty segment
        secrets.encrypt(seg)
    }
}
