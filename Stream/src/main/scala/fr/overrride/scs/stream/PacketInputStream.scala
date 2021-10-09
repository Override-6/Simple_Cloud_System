package fr.overrride.scs.stream

import fr.overrride.scs.stream.packet.Packet
import fr.overrride.scs.stream.packet.exception.UnexpectedPacketException

import java.io.{InputStream, ObjectInputStream}

class PacketInputStream(in: InputStream) extends ObjectInputStream(in) {

    def readPacket(): Packet = {
        val packet = readObject()
        packet match {
            case p: Packet => p
            case obj       => throw new UnexpectedPacketException(s"Received unexpected object of type ${obj.getClass.getName} which does implements the ${classOf[Packet].getName} trait.")
        }
    }

    def readPackets(buff: Array[Packet]): Unit = {
        for (i <- buff.indices)
            buff(i) = readPacket()
    }
}
