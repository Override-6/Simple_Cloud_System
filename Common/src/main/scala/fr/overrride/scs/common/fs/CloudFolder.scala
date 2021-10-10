package fr.overrride.scs.common.fs

import java.nio.file.Path

/**
 * Represents a folder in the client's cloud space (also called as distant folder)
 * A folder can perform some actions in it, such as modifying itself or
 * retrieve information about its contained items.
 * */
trait CloudFolder extends CloudItem {

    /**
     * Creates an empty file into this cloud folder
     * */
    def createFile(name: String): Unit

    /**
     * Creates an empty folder into this cloud folder
     * */
    def createFolder(name: String): Unit

    /**
     * Uploads a folder into this cloud folder
     * @param folderName the name of the distant subfolder to create and fill
     * @param source the source path of the folder that will be sent to the distant folder
     */
    def uploadFolder(folderName: String, source: Path): Unit

    /**
     * Downloads a folder from this cloud folder
     * @param folderName the name of the distant subfolder to download
     * @param dest the source path of the folder in which the sub folders and sub files will be stored
     * */
    def downloadFolder(folderName: String, dest: Path): Unit

    /**
     * Uploads a file into this cloud folder
     * @param name the name of the distant subfolder to create and fill
     * @param source the source path of the folder that will be sent to the distant folder
     */
    def uploadFile(name: String, source: Path): Unit

    /**
     * Downloads a file from this cloud folder
     * @param name the name of the distant subfolder to download
     * @param dest the source path of the folder in which the sub folders and sub files will be stored.
     */
    def downloadFile(name: String, dest: Path): Unit

    /**
     * @return an array containing the current items in the cloud folder
     */
    def getAvailableItems: Array[CloudItem]

    /**
     * @param name the item name
     * @return Some(CloudItem) if the name matches a contained item, None instead
     * */
    def findItem(name: String): Option[CloudItem]


}
