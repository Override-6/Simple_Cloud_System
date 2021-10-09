package fr.overrride.scs.common.fs

import java.nio.file.Path

object PathOps {

    implicit class SuperPath(self: Path) {

        def /(other: Path): Path = Path.of(self + "/" + other)

        def /(other: String): Path = Path.of(self + "/" + other)
    }


}
