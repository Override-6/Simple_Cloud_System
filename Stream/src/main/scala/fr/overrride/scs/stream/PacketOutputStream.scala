package fr.overrride.scs.stream

import fr.overrride.scs.common.packet.Packet
import fr.overrride.scs.stream.packet.PacketSerializer

import java.io.{BufferedOutputStream, OutputStream}
import java.nio.ByteBuffer

class PacketOutputStream(out: OutputStream) extends BufferedOutputStream(out) {

    private val buff = ByteBuffer.allocate(15000)

    @inline
    def writePacket(packet: Packet): Unit = buff.synchronized {
        buff.position(4)
        new PacketSerializer(buff).serialize(packet)
        val pos = buff.position()
        buff.putInt(0, pos - 4) //write the packet length
        write(buff.array(), 0, pos)
        flush()
    }

    def writePackets(packets: Array[Packet]): Unit = {
        for (p <- packets)
            writePacket(p)
    }
}
