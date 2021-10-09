package fr.overrride.scs.stream.packet

case class FileSegment(fileName: String, len: Int, segment: Array[Byte]) extends Packet {

}
