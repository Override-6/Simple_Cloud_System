package fr.overrride.scs.stream.packet

object ProtocolConstants {

    final val Boolean                  : Byte = 1
    final val FileStoreItemInfo        : Byte = 2
    final val FileDownloadReq          : Byte = 3
    final val FileUploadReq            : Byte = 4
    final val FileStoreFolderContentReq: Byte = 5
    final val FileStoreItemReq         : Byte = 6
    final val StringMsg                : Byte = 7
    final val FileSegment              : Byte = 8
    final val EOF                      : Byte = 9
    final val None                : Byte = 10
    final val FileStoreContentResp: Byte = 11
}
