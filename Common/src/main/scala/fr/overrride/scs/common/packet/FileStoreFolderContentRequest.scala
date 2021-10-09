package fr.overrride.scs.common.packet

import fr.overrride.scs.common.fs.FileStoreItemInfo

case class FileStoreFolderContentRequest(folderInfo: FileStoreItemInfo) extends Packet