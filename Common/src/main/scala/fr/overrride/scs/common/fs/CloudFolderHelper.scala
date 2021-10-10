package fr.overrride.scs.common.fs

import java.nio.file.{Files, Path}

/**
 * Utilities methods for [[CloudFolder]] implementations
 * */
object CloudFolderHelper {

    /**
     * Ensures that the given path targets a file
     * @param path the path to check
     * @param createIfNotExist creates the file if the path points to an non-existent file
     * @throws IllegalArgumentException if the file do not exists and `createIfNotExist == false`,
     *                                  or if the file is a directory
     */
    def ensureFile(path: Path, createIfNotExist: Boolean = true): Unit = {
        if (createIfNotExist && Files.notExists(path)) {
            Files.createDirectories(path.getParent)
            Files.createFile(path)
            return
        }
        if (!createIfNotExist && Files.notExists(path))
            throw new IllegalArgumentException(s"source $path does not exists")
        if (Files.isDirectory(path))
            throw new IllegalArgumentException(s"source $path is not a file.")
    }

    /**
     * Ensures that the given path targets a folder
     * @param path the path to check*
     * @throws IllegalArgumentException if the file is not a directory
     */
    def ensureFolder(path: Path): Unit = {
        if (Files.notExists(path))
            Files.createDirectories(path)
        if (!Files.isDirectory(path))
            throw new IllegalArgumentException(s"path $path is not a folder.")
    }

    /**
     * Appends the itemName with info.relativePath
     * */
    def relativize(itemName: String)(implicit info: CloudItemInfo): String = {
        val path = info.relativePath
        val name = itemName.dropWhile(_ == '/')
        if (path.endsWith("/"))
            path + name
        else
            path + "/" + name
    }

}
