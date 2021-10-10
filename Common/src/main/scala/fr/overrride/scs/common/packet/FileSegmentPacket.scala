package fr.overrride.scs.common.packet

case class FileSegmentPacket(fileName: String, segment: Array[Byte]) extends Packet {

}
