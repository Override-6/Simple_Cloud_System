package fr.overrride.scs.common.packet

/**
 * Represents one data segment of a file
 * */
case class FileSegmentPacket(fileName: String, segment: Array[Byte]) extends Packet