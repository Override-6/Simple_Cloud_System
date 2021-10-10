package fr.overrride.scs.stream

import fr.overrride.scs.common.packet.Packet

import java.io.{ObjectOutputStream, OutputStream}

/**
 * Writes packets in an output stream
 * */
class PacketOutputStream(out: OutputStream) extends ObjectOutputStream(out) {

    /**
     * @param packet the packet to write in the output stream
     * */
    @inline
    def writePacket(packet: Packet): Unit = {
        writeObject(packet)
    }

}
