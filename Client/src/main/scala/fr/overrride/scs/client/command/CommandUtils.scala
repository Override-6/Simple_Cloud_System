/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can only use it for personal uses, studies or documentation.
 * You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.overrride.scs.client.command

import fr.overrride.scs.common.fs.{FileStoreFolder, FileStoreItem}

object CommandUtils {

    @throws[CommandException]("if expected argument not found in provided args.")
    def ensureArgsContains(expected: String*)(implicit args: Array[String]): Unit = {
        val success = expected.forall(args.contains)
        if (success)
            return
        val errorMsg = s"Missing or wrong argument in command syntax. Expected : ${expected.mkString(" and ")}"
        throw CommandException(errorMsg)
    }

    def argAfter(ref: String)(implicit args: Array[String]): String =
        args(args.indexOf(ref) + 1)

    def getValue(name: String, default: String, args: Array[String]): String = {
        args.foreach(arg => {
            val pair = arg.split('=')
            if (pair(0) == name && pair.length == 2)
                return pair(1)
        })
        default
    }

    def getValue(name: String, args: Array[String])(implicit usage: String): String = {
        args.foreach(arg => {
            val pair = arg.split('=')
            if (pair(0) == name && pair.length == 2)
                return pair(1)
        })
        throw CommandException(s"Missing argument '$name=?', " + usage)
    }

    def getFolder(root: FileStoreFolder, relativePath: String): FileStoreFolder = {
        getItem(root, relativePath) match {
            case folder: FileStoreFolder => folder
            case _                       => throw CommandException(s"$relativePath is not a folder.")
        }
    }

    def getItem(root: FileStoreFolder, relativePath: String): FileStoreItem = {
        val names                   = relativePath.split("/")
        var folder: FileStoreFolder = root
        val len                     = names.length
        for (i <- names.indices) {
            folder.findItem(names(i)) match {
                case None        =>
                    throw CommandException(s"Folder ${names.take(i).mkString("/")} does not exists.")
                case Some(value) =>
                    value match {
                        case f: FileStoreFolder                   => folder = f
                        case other: FileStoreItem if i == len - 1 =>
                            return other //We hit the last item, which is a file so we return the file item.
                        case other: FileStoreItem                 =>
                            //we hit an item, but the given path was not fully iterated.
                            throw CommandException(s"${other.info.relativePath} is not a folder.")
                    }
            }
        }
        folder
    }
}
