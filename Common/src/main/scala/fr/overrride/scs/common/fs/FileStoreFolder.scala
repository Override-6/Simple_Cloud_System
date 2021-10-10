package fr.overrride.scs.common.fs

import java.nio.file.Path

trait FileStoreFolder extends FileStoreItem {

    def createFile(name: String): Unit

    def createFolder(name: String): Unit

    def uploadFolder(folderName: String, source: Path): Unit

    def downloadFolder(folderName: String, dest: Path): Unit

    def uploadFile(name: String, source: Path): Unit

    def downloadFile(name: String, dest: Path): Unit

    def getAvailableItems: Array[FileStoreItem]

    def findItem(name: String): Option[FileStoreItem]


}
