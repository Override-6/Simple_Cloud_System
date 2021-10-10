package fr.overrride.scs.common.fs

/**
 * Depicts an item (file or folder) on the server's storage
 * */
trait CloudItem {

    /**
     * The information on the cloud item
     */
    val info: CloudItemInfo
}
