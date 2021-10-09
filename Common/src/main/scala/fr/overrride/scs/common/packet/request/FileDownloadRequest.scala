package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

case class FileDownloadRequest(relativePath: String) extends Packet {

}
