package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

/**
 * Sent to the server to start a file upload
 * @param relativeFilePath the path from the sender cloud space's root to the targeted file
 * */
case class FileUploadRequest(relativeFilePath: String) extends Packet