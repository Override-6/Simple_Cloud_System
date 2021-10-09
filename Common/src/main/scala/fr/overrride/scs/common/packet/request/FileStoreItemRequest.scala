package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

case class FileStoreItemRequest(relativePath: String) extends Packet
