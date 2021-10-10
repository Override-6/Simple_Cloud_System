package fr.overrride.scs.common.fs

import java.nio.file.Path

/**
 * Utility class in order to ease append operations on [[Path]]
 * */
object PathOps {

    implicit class AppendPath(self: Path) {

        def /(other: Path): Path = Path.of(self + "/" + other)

        def /(other: String): Path = Path.of(self + "/" + other)
    }


}
