package fr.overrride.scs.stream

import fr.overrride.scs.stream.packet.exception.{FileCollapseException, UnexpectedPacketException}
import fr.overrride.scs.stream.packet.{EOFPacket, FileInfoPacket, FileSegment}

import java.io.OutputStream
import java.nio.file.attribute.FileTime
import java.nio.file.{Files, NotDirectoryException, Path}
import java.time.Instant
import java.util.concurrent.TimeUnit

class RemoteFileReader(in: PacketInputStream) {

    def readFile(outputFolder: Path): Int = {
        if (!Files.isDirectory(outputFolder))
            throw new NotDirectoryException(s"$outputFolder is not a directory.")
        val info = nextFileInfo()
        val read = readFileContent(Files.newOutputStream(outputFolder), info)
        Files.setLastModifiedTime(outputFolder, new FileTime(info.lastModified, TimeUnit.MILLISECONDS, Instant.now()))
        read
    }

    private def readFileContent(out: OutputStream, info: FileInfoPacket): Int = {
        var next     = in.readPacket()
        var total    = 0
        val fileName = info.name
        while (true) {
            next match {
                case EOFPacket                     => return total
                case FileSegment(fn, len, segment) =>
                    if (fn != info.name)
                        throw new FileCollapseException(s"Received foreign file fragment while downloading file $fileName. (received fragment from file $fn)")
                    writeContent(out, segment, len)
                    total += segment.length
                    next = in.readPacket()
            }
        }
        total
    }

    protected def writeContent(out: OutputStream, segment: Array[Byte], len: Int): Unit = {
        out.write(segment, 0, len)
    }

    private def nextFileInfo(): FileInfoPacket = {
        in.readPacket() match {
            case info: FileInfoPacket => info
            case other                => throw new UnexpectedPacketException(s"Received unexpected packet of type ${other.getClass.getName}, expected ${classOf[FileInfoPacket].getSimpleName}.")
        }
    }

}
