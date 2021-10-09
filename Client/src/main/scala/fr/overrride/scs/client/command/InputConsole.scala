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

import java.util.concurrent.PriorityBlockingQueue
import scala.io.StdIn

object InputConsole {

    private val ticketQueue = new PriorityBlockingQueue[InputRequestTicket]()
    start()

    def requestNextInput(priority: Int = 0): String = {
        val requestTicket = new InputRequestTicket(defNextPriority(priority))
        ticketQueue.add(requestTicket)
        requestTicket.getLine
    }

    private def defNextPriority(priority: Int): Int = {
        var queuePriority = priority
        ticketQueue.forEach(incPriority)

        def incPriority(ticket: InputRequestTicket): Unit = {
            if (ticket.priority < priority)
                return
            if (ticket.priority == queuePriority)
                queuePriority += 1
        }

        queuePriority
    }


    private def start(): Unit = {
        val consoleThread = new Thread(() => {
            while (true) {
                val ticket = ticketQueue.take()
                val line = StdIn.readLine()
                ticket.setLine(line)
            }
        })
        consoleThread.setName("Console Inputs Queue")
        consoleThread.setDaemon(true)
        consoleThread.start()
    }


    private class InputRequestTicket(val priority: Int) extends Comparable[InputRequestTicket] {
        private val threadOwner = Thread.currentThread()
        @volatile private var line: String = _

        def getLine: String = {
            if (line == null)
                synchronized {
                    wait()
                }
            line
        }

        def setLine(line: String): Unit = {
            this.line = line
            synchronized {
                notify()
            }
        }

        override def toString: String = s"owner : $threadOwner"

        override def compareTo(o: InputRequestTicket): Int =
            o.priority - priority
    }

}
