package fr.overrride.scs.stream

import fr.overrride.scs.common.packet.Packet

import java.io.{ObjectOutputStream, OutputStream}

class PacketOutputStream(out: OutputStream) extends ObjectOutputStream(out) {

    @inline
    def writePacket(packet: Packet): Unit = {
        writeObject(packet)
    }

    def writePackets(packets: Array[Packet]): Unit = {
        for (p <- packets)
            writePacket(p)
    }
}
