package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

case class FileStoreFolderContentRequest(relativePath: String) extends Packet
