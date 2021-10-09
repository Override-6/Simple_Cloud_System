package fr.overrride.scs.client.command

import scala.io.StdIn

object CommandListener extends AutoCloseable {

    private var closed = false
    private var open   = false

    def listen(): Unit = {
        if (closed || open)
            throw new IllegalStateException("CommandListener is closed or opened")
        open = true
        while (open)
            StdIn.readLine()
    }

}
