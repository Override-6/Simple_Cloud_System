package fr.overrride.scs.stream

import fr.overrride.scs.common.fs.FileStoreItemInfo
import fr.overrride.scs.common.packet.{EOFPacket, FileSegmentPacket, ObjectPacket}
import fr.overrride.scs.stream.RemoteFileWriter.SegmentSize

import java.io.InputStream
import java.nio.file.{Files, Path}

class RemoteFileWriter(out: PacketOutputStream) {

    def writeFile(source: Path, cloudPath: String): Unit = {
        val info = writeFileInfo(source, cloudPath)
        writeFileContent(source, info)
    }

    private def writeFileContent(source: Path, info: FileStoreItemInfo): Unit = {
        val in      = Files.newInputStream(source)
        val size    = Math.min(in.available(), SegmentSize)
        var content = readContent(in, size)
        while (content.nonEmpty) {
            out.writePacket(FileSegmentPacket(info.relativePath, content))
            content = readContent(in, size)
        }
        out.writePacket(EOFPacket)
    }

    protected def readContent(in: InputStream, len: Int): Array[Byte] = {
        val buff  = new Array[Byte](len)
        val count = in.read(buff)
        if (count == -1)
            return Array.empty
        val result = new Array[Byte](count)
        System.arraycopy(buff, 0, result, 0, count)
        result
    }

    private def writeFileInfo(file: Path, remotePath: String): FileStoreItemInfo = {
        val info = FileStoreItemInfo(remotePath, Files.isDirectory(file), Files.getLastModifiedTime(file).toMillis)
        out.writePacket(ObjectPacket(info))
        info
    }

}

object RemoteFileWriter {

    private final val SegmentSize: Int = 150000
}
