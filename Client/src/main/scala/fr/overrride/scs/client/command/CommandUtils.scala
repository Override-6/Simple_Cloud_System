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

import java.util.regex.Pattern

object CommandUtils {

    private val PathRegex = Pattern.compile("[/\\\\]+")

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

    def getFolder(root: FileStoreFolder, relativePath: String, createIfNotExists: Boolean): FileStoreFolder = {
        getItem(root, relativePath, createIfNotExists) match {
            case folder: FileStoreFolder => folder
            case _                       => throw CommandException(s"$relativePath is not a folder.")
        }
    }

    def getItem(root: FileStoreFolder, relativePath: String, createIfNotExists: Boolean): FileStoreItem = {
        if (relativePath.isEmpty)
            return root
        val names                   = PathRegex.split(relativePath.dropWhile(_ == '/'))
        var folder: FileStoreFolder = root
        val len                     = names.length
        for (i <- names.indices) {
            val name = names(i)
            folder.findItem(name) match {
                case None if createIfNotExists =>
                    if (i == len && name.contains('.')) folder.createFile(name)
                    else folder.createFolder(name)
                case None                      =>
                    throw CommandException(s"Folder|file ${names.mkString("/")} does not exists.")
                case Some(value)               =>
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
