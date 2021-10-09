package fr.overrride.scs.stream

import fr.overrride.scs.common.packet.{EOFPacket, FileSegment, ObjectPacket}
import fr.overrride.scs.stream.packet.ObjectPacket

import java.io.InputStream
import java.nio.file.{Files, Path}

class RemoteFileWriter(out: PacketOutputStream) {

    def writeFile(path: Path, segmentSize: Int): Unit = {
        val info = writeFileInfo(path)
        writeFileContent(path, info, segmentSize)
    }

    private def writeFileContent(path: Path, info: ObjectPacket, segmentSize: Int): Unit = {
        val buff  = new Array[Byte](segmentSize)
        val in    = Files.newInputStream(path)
        var count = 0
        while (count != -1) {
            count = readContent(in, buff)
            out.writePacket(FileSegment(info.name, count, buff))
        }
        out.writePacket(EOFPacket)
    }

    protected def readContent(in: InputStream, buff: Array[Byte]): Int = in.read(buff)

    private def writeFileInfo(file: Path): ObjectPacket = {
        val packet = FileInfoPacket(file.getFileName.toString, Files.getLastModifiedTime(file).toMillis)
        out.writePacket(packet)
        packet
    }
}
