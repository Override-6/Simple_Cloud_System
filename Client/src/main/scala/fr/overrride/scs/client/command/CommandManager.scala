package fr.overrride.scs.client.command

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.StdIn
import scala.util.control.NonFatal

/**
 * Stores and manages all the registered [[CommandExecutor]]
 * */
class CommandManager extends AutoCloseable {

    private val commands: mutable.Map[String, CommandExecutor] = mutable.Map.empty
    @volatile private var alive                                = true

    /**
     * Closes the manager, this will have for effect to release the console input.
     * */
    override def close(): Unit = {
        commands.clear()
        alive = false
    }

    /**
     * Register a command
     * @param commandName the name of the command.
     * @param executor the executor object to execute when the commandName is entered in the console)
     * */
    def register(commandName: String, executor: CommandExecutor): Unit =
        commands.put(commandName.toLowerCase, executor)

    /**
     * Starts the Console input listening.
     * The input will constantly get listened in order to track user commands.
     */
    def start(): Unit = {
        while (alive)
            perform(StdIn.readLine())
    }

    /**
     * performs and parse a raw command line
     * @param command the command line to execute
     * */
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

    /**
     * turns a string line to an array of strings.
     * The resulted array is a split for every blanc character, excluding ones put between quotes ("")
     * Example : download "example/My File.txt" -dest  C:/Storage/Example
     * turns to [download, example/My File.txt, -dest, C:/Storage/Example]
     * @param line the line to parse
     * */
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
