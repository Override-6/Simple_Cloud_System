package fr.overrride.scs.stream

import fr.overrride.scs.common.fs.FileStoreItemInfo
import fr.overrride.scs.common.packet.{EOFPacket, FileSegmentPacket, FileStoreItemInfoPacket}

import java.io.InputStream
import java.nio.file.{Files, Path}

class RemoteFileWriter(out: PacketOutputStream) {

    def writeFile(source: Path, cloudPath: String, segmentSize: Int): Unit = {
        val info = writeFileInfo(source, cloudPath)
        writeFileContent(source, info, segmentSize)
    }

    private def writeFileContent(source: Path, info: FileStoreItemInfo, segmentSize: Int): Unit = {
        val buff  = new Array[Byte](segmentSize)
        val in    = Files.newInputStream(source)
        var count = 0
        while (count != -1) {
            count = readContent(in, buff)
            out.writePacket(FileSegmentPacket(info.relativePath, count, buff))
        }
        out.writePacket(EOFPacket)
    }

    protected def readContent(in: InputStream, buff: Array[Byte]): Int = in.read(buff)

    private def writeFileInfo(file: Path, remotePath: String): FileStoreItemInfo = {
        val info = FileStoreItemInfo(remotePath, Files.isDirectory(file), Files.getLastModifiedTime(file).toMillis)
        out.writePacket(FileStoreItemInfoPacket(info))
        info
    }
}
