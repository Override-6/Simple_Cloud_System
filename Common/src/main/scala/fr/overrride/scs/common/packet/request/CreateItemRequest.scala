package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

case class CreateItemRequest(itemName: String, isFolder: Boolean) extends Packet