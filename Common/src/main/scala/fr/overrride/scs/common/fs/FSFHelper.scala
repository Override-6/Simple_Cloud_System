package fr.overrride.scs.common.fs

import java.nio.file.{Files, Path}

object FSFHelper {

    def ensureFile(path: Path): Unit = {
        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent)
            Files.createFile(path)
        }
        if (Files.isDirectory(path))
            throw new IllegalArgumentException(s"source $path is not a file.")
    }

    def ensureFolder(path: Path): Unit = {
        if (Files.notExists(path))
            Files.createDirectories(path)
        if (!Files.isDirectory(path))
            throw new IllegalArgumentException(s"path $path is not a folder.")
    }

    def relativize(fileName: String)(implicit info: FileStoreItemInfo): String = info.relativePath + "/" + fileName

}
