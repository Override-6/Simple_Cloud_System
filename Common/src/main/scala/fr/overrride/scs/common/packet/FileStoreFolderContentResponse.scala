package fr.overrride.scs.common.packet

import fr.overrride.scs.common.fs.FileStoreItemInfo

case class FileStoreFolderContentResponse(folderInfo: FileStoreItemInfo, itemInfos: Array[FileStoreItemInfo]) extends Packet