package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

/**
 * Sent to the server to start a file download
 * @param relativePath the path from the sender cloud space's root to the targeted file
 * */
case class FileDownloadRequest(relativePath: String) extends Packet