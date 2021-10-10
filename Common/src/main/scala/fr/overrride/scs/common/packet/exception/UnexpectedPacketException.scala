package fr.overrride.scs.common.packet.exception

/**
 * Thrown when an unexpected packet is received (can be an unexpected packet type, or an unexpected content)
 * */
class UnexpectedPacketException(msg: String) extends PacketException(msg)