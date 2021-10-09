package fr.overrride.scs.stream.packet

import fr.overrride.scs.common.fs
import fr.overrride.scs.common.packet._
import fr.overrride.scs.common.packet.request._
import fr.overrride.scs.stream.packet.ProtocolConstants._

import java.nio.ByteBuffer

class PacketSerializer(buff: ByteBuffer) {

    def serialize(packet: Packet): Unit = {
        packet match {
            case FileStoreItemInfoPacket(info)             => writeFlag(FileStoreItemInfo); writeFileInfo(info)
            case FileSegmentPacket(fileName, len, segment) => writeFlag(FileSegment); writeString(fileName); buff.putInt(len); buff.put(segment)
            case BooleanPacket(bool)                       => writeFlag(Boolean); writeBool(bool)
            case FileDownloadRequest(path)                 => writeFlag(FileDownloadReq); writeString(path)
            case FileUploadRequest(path)                   => writeFlag(FileUploadReq); writeString(path)
            case FileStoreFolderContentRequest(path)       => writeFlag(FileStoreFolderContentReq); writeString(path)
            case FileStoreItemRequest(path)                => writeFlag(FileStoreItemReq); writeString(path)
            case StringPacket(str)                         => writeFlag(StringMsg); writeString(str)
            case EOFPacket                                 => writeFlag(EOF)
            case NonePacket                                => writeFlag(None)
            case FileStoreContentResponse(items)           =>
                writeFlag(FileStoreContentResp)
                buff.putInt(items.length)
                items.foreach(writeFileInfo)
        }
    }

    private def writeFileInfo(info: fs.FileStoreItemInfo): Unit = {
        writeString(info.relativePath)
        writeBool(info.isFolder)
        buff.putLong(info.lastModified)
    }

    private def writeFlag(flag: Byte): Unit = {
        buff.put(flag)
    }

    private def writeString(str: String): Unit = {
        buff.putInt(str.length)
        buff.put(str.getBytes())
    }

    private def writeBool(bool: Boolean): Unit = {
        buff.put((if (bool) 1 else 0): Byte)
    }

}
