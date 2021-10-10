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

/**
 * Thrown to fail a command: This command should only be used into [[CommandExecutor.execute]] method.
 * When the exception is catch by the [[CommandManager]], the error message is print without the statck trace.
 * This command should only be used in order to fail a command and print an error message in the command.
 * */
case class CommandException(msg: String) extends RuntimeException(msg)
