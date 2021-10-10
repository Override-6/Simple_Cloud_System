package fr.overrride.scs.common.fs

/**
 * Exception thrown when something went wrong during a file or folder transfer
 * */
class FileTransferException(msg: String, cause: Throwable = null) extends Exception(msg, cause)
