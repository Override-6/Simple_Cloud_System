package fr.overrride.scs.stream

import fr.overrride.scs.common.fs.CloudItemInfo
import fr.overrride.scs.common.packet.exception.{FileCollapseException, UnexpectedPacketException}
import fr.overrride.scs.common.packet.{EOFPacket, FileSegmentPacket, ObjectPacket}

import java.io.OutputStream
import java.nio.file.attribute.FileTime
import java.nio.file.{Files, NotDirectoryException, Path}

class RemoteFileReader(in: PacketInputStream) {
    def readFile(dest: Path): Int = {
        if (Files.isDirectory(dest))
            throw new NotDirectoryException(s"$dest is a directory.")
        val info = nextFileInfo()
        val read = readFileContent(Files.newOutputStream(dest), info)
        Files.setLastModifiedTime(dest, FileTime.fromMillis(info.lastModified))
        read
    }

    private def readFileContent(out: OutputStream, info: CloudItemInfo): Int = {
        var next  = in.readPacket()
        var total = 0
        val path  = info.relativePath
        while (true) {
            next match {
                case EOFPacket                           =>
                    out.close()
                    return total
                case FileSegmentPacket(fn, segment) =>
                    if (fn != info.relativePath)
                        throw new FileCollapseException(s"Received foreign file fragment while downloading file $path. (received fragment from file $fn)")
                    writeContent(out, segment)
                    total += segment.length
                    next = in.readPacket()
            }
        }
        total //impossible
    }

    protected def writeContent(out: OutputStream, segment: Array[Byte]): Unit = {
        out.write(segment)
    }

    private def nextFileInfo(): CloudItemInfo = {
        in.readPacket() match {
            case ObjectPacket(info: CloudItemInfo) => info
            case other                             =>
                throw new UnexpectedPacketException(s"Received unexpected packet of type ${other.getClass.getName}, expected ObjectPacket(FileStoreItemInfo).")
        }
    }

}
