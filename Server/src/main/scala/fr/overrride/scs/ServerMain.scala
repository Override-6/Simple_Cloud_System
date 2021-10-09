package fr.overrride.scs

import java.nio.file.{Files, Path}

object ServerMain {

    def main(args: Array[String]): Unit = {
        val workFolder = Path.of(getOrElse(args, "folder", "/UserFiles/"))
        if (!Files.isDirectory(workFolder))
            throw new InvalidConfigurationException(s"working folder (${workFolder}) is not a directory.")
        if (Files.notExists(workFolder))
            Files.createDirectories(workFolder)

    }

    //noinspection SameParameterValue
    private def getOrElse(args: Array[String], key: String, defaultValue: String): String = {
        val index = args.indexOf(key)
        if (index < 0 || index + 1 > args.length - 1) {
            defaultValue
        } else {
            args(index + 1)
        }
    }
}
