package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

/**
 * Sent to the server in order to retrieve the [[fr.overrride.scs.common.fs.CloudItemInfo]] of a specified item path, if any
 * @param relativePath the path from the sender cloud space's root to the targeted file
 */
case class CloudItemRequest(relativePath: String) extends Packet