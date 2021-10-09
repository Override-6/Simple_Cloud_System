package fr.overrride.scs.common.packet

case class FileSegment(fileName: String, len: Int, segment: Array[Byte]) extends Packet {

}
