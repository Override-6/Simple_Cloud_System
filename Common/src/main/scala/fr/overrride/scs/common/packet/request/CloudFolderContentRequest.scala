package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

/**
 * Sent to the server in order to request an array of [[fr.overrride.scs.common.fs.CloudItemInfo]]
 * which represents the targeted folder's items information
 * @param relativePath the path from the sender cloud space's root to the targeted file
 */
case class CloudFolderContentRequest(relativePath: String) extends Packet
