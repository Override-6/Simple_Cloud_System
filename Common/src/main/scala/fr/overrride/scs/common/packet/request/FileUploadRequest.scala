package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.packet.Packet

case class FileUploadRequest(relativeFilePath: String) extends Packet