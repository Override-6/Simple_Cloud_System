package fr.overrride.scs.stream

import fr.overrride.scs.common.packet.Packet
import fr.overrride.scs.stream.packet.PacketDeserializer

import java.io.{BufferedInputStream, InputStream}
import java.nio.ByteBuffer

class PacketInputStream(in: InputStream) extends BufferedInputStream(in) {

    def readPacket(): Packet = {
        val len  = nextInt()
        val buff = ByteBuffer.allocate(len)
        read(buff.array())
        new PacketDeserializer(buff).deserialize()
    }

    def readPackets(buff: Array[Packet]): Unit = {
        for (i <- buff.indices)
            buff(i) = readPacket()
    }

    private def nextInt(): Int = {
        val bytes = new Array[Byte](4)
        in.read(bytes)
        (0xff & bytes(0)) << 24 |
                ((0xff & bytes(1)) << 16) |
                ((0xff & bytes(2)) << 8) |
                ((0xff & bytes(3)) << 0)
    }
}
