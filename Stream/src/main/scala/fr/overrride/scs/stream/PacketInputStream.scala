package fr.overrride.scs.stream

import fr.overrride.scs.common.packet.Packet
import fr.overrride.scs.common.packet.exception.UnexpectedPacketException

import java.io.{InputStream, ObjectInputStream}

/**
 * Reads packets from an input stream
 * */
class PacketInputStream(in: InputStream) extends ObjectInputStream(in) {

    /**
     * @return the next [[Packet]] object
     */
    def readPacket(): Packet = {
        val packet = readObject()
        packet match {
            case p: Packet => p
            case obj       => throw new UnexpectedPacketException(s"Received unexpected object of type ${obj.getClass.getName} which does implements the ${classOf[Packet].getName} trait.")
        }
    }

}
