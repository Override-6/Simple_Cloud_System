package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

/**
 * Sent to the server to create a empty folder or file into the sender's cloud space
 * @param itemName the name of the item
 * @param isFolder `true` if the item must be a folder, `false` if it must be a file
 * */
case class CreateItemRequest(itemName: String, isFolder: Boolean) extends Packet