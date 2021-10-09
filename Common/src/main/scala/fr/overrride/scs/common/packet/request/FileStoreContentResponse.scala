package fr.overrride.scs.common.packet.request

import fr.overrride.scs.common.fs.FileStoreItemInfo
import fr.overrride.scs.common.packet.Packet

case class FileStoreContentResponse(items: Array[FileStoreItemInfo]) extends Packet