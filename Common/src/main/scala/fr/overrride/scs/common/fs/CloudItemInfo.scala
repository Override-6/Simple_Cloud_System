package fr.overrride.scs.common.fs

/**
 * Required information of a [[CloudItem]]
 * @param relativePath the relative path, starting from the root, of the item
 * @param isFolder `true` if the item is a folder
 * @param lastModified last time the item was modified, since January 1, 1970, 00:00:00 GMT.
 */
case class CloudItemInfo(relativePath: String, isFolder: Boolean, lastModified: Long = -1)