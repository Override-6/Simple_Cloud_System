package fr.overrride.scs.stream.packet

import fr.overrride.scs.common.fs
import fr.overrride.scs.common.packet._
import fr.overrride.scs.common.packet.request._
import fr.overrride.scs.stream.packet.ProtocolConstants._

import java.nio.ByteBuffer
import scala.annotation.switch

class PacketDeserializer(buff: ByteBuffer) {

    def deserialize(): Packet = {
        (buff.get(): @switch) match {
            case Boolean                   => BooleanPacket(readBool())
            case FileStoreItemInfo         => FileStoreItemInfoPacket(readFileInfo())
            case FileDownloadReq           => FileDownloadRequest(readString())
            case FileUploadReq             => FileUploadRequest(readString())
            case FileStoreFolderContentReq => FileStoreFolderContentRequest(readString())
            case FileStoreItemReq          => FileStoreItemRequest(readString())
            case StringMsg                 => StringPacket(readString())
            case EOF                       => EOFPacket
            case None                      => NonePacket
            case FileStoreContentResp      =>
                val content = new Array[fs.FileStoreItemInfo](buff.getInt).mapInPlace(_ => readFileInfo())
                FileStoreContentResponse(content)
            case FileSegment               =>
                val segmentBuff = new Array[Byte](buff.getInt)
                buff.get(segmentBuff)
                FileSegmentPacket(readString(), segmentBuff.length, segmentBuff)
        }
    }

    private def readFileInfo(): fs.FileStoreItemInfo = {
        fs.FileStoreItemInfo(readString(), readBool(), buff.getLong)
    }

    private def readString(): String = {
        val content = new Array[Byte](buff.getInt)
        buff.get(content)
        new String(content)
    }

    private def readBool(): Boolean = buff.get() == 1

}

object PacketDeserializer {

}
