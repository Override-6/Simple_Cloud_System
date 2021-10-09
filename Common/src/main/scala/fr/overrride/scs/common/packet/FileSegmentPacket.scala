package fr.overrride.scs.common.packet

case class FileSegmentPacket(fileName: String, len: Int, segment: Array[Byte]) extends Packet {

}
