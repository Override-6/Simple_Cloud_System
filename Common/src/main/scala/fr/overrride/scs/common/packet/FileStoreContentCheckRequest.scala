package fr.overrride.scs.common.packet

import fr.overrride.scs.common.fs.FileStoreItemInfo

case class FileStoreContentCheckRequest(folderInfo: FileStoreItemInfo, itemPath: String) extends Packet