package fr.overrride.scs.client.command

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

class CommandManager extends AutoCloseable {

    private val commands: mutable.Map[String, CommandExecutor] = mutable.Map.empty
    @volatile private var alive                                = true

    override def close(): Unit = {
        commands.clear()
        alive = false
    }

    def register(commandName: String, executor: CommandExecutor): Unit =
        commands.put(commandName.toLowerCase, executor)

    def start(): Unit = {
        while (alive)
            perform(InputConsole.requestNextInput())
    }

    def perform(command: String): Unit = {
        if (command == null)
            return
        if (command.startsWith("help")) {
            commands.foreach(cmd => println(cmd._1 + " -> " + cmd._2.getClass.getSimpleName))
            return
        }
        val args = parseLine(command.trim())
        val cmd  = command.takeWhile(c => !Character.isWhitespace(c)).toLowerCase
        if (!commands.contains(cmd)) {
            Console.err.println(s"cmd '$cmd' not found.")
            return
        }

        try {
            commands(cmd).execute(args)
        } catch {
            case e@(_: CommandException) => Console.err.println(e.getMessage)
            case NonFatal(e)             => e.printStackTrace()
        }
    }

    private def parseLine(line: String): Array[String] = {
        val argBuilder = new StringBuilder
        val args       = ListBuffer.empty[String]

        //exclude first arg, which is the command label
        val indexOfFirstBlankLine = line.indexWhere(Character.isWhitespace)
        if (indexOfFirstBlankLine == -1)
            return Array()
        val rawArgs = line.substring(indexOfFirstBlankLine).trim()

        var insideString = false
        var last         = '\u0000'
        for (c <- rawArgs) {
            if (c == '"' && last != '\\')
                insideString = !insideString
            else if (!c.isWhitespace || (insideString && last != '\\'))
                argBuilder.append(c)
            else if (!last.isWhitespace) {
                args += argBuilder.toString()
                argBuilder.clear()
            }
            last = c
        }
        args += argBuilder.toString()
        args.toArray
    }

}
