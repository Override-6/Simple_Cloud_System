package fr.overrride.scs.stream

import fr.overrride.scs.common.fs.FileStoreItemInfo
import fr.overrride.scs.common.packet.{EOFPacket, FileSegment, ObjectPacket}

import java.io.InputStream
import java.nio.file.{Files, Path}

class RemoteFileWriter(out: PacketOutputStream) {

    def writeFile(path: Path, remotePath: String, segmentSize: Int): Unit = {
        val info = writeFileInfo(path, remotePath)
        writeFileContent(path, info, segmentSize)
    }

    private def writeFileContent(path: Path, info: FileStoreItemInfo, segmentSize: Int): Unit = {
        val buff  = new Array[Byte](segmentSize)
        val in    = Files.newInputStream(path)
        var count = 0
        while (count != -1) {
            count = readContent(in, buff)
            out.writePacket(FileSegment(info.relativePath, count, buff))
        }
        out.writePacket(EOFPacket)
    }

    protected def readContent(in: InputStream, buff: Array[Byte]): Int = in.read(buff)

    private def writeFileInfo(file: Path, remotePath: String): FileStoreItemInfo = {
        val info = FileStoreItemInfo(remotePath, Files.isDirectory(file), Files.getLastModifiedTime(file).toMillis)
        out.writePacket(ObjectPacket(info))
        info
    }
}
