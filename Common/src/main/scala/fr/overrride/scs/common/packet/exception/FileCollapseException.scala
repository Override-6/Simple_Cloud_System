package fr.overrride.scs.common.packet.exception

/**
 * Thrown when a [[fr.overrride.scs.common.packet.FileSegmentPacket]] is received but targets
 * the wrong file.
 * */
class FileCollapseException(msg: String, cause: Throwable = null) extends Exception(msg, cause)