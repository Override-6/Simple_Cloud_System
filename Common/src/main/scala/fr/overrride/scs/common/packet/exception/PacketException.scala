package fr.overrride.scs.common.packet.exception

/**
 * base class for all exceptions that concerns packet traffic
 * */
class PacketException(msg: String, cause: Throwable = null) extends Exception(msg, cause)