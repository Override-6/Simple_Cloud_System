package fr.overrride.scs.stream

import fr.overrride.scs.stream.packet.Packet
import fr.overrride.scs.stream.packet.exception.{MalformedPacketException, UnexpectedObjectException}

import java.io.{InputStream, ObjectInputStream}

class PacketInputStream(in: InputStream) extends ObjectInputStream(in) {

    def readPacket(): Packet = {
        val len = readInt()
        if (len < 0)
            throw new MalformedPacketException(s"Received negative packet length ($len).")
        val packet = readObject()
        packet match {
            case p: Packet => p
            case obj       => throw new UnexpectedObjectException(s"Received unexpected object of type ${obj.getClass.getName} which does implements the ${classOf[Packet].getName} trait.")
        }
    }


    def readPackets(buff: Array[Packet]): Unit = {
        for (i <- buff.indices)
            buff(i) = readPacket()
    }
}
