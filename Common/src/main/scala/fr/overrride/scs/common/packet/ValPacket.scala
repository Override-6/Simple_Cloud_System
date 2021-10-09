package fr.overrride.scs.common.packet

import scala.Specializable.Primitives

case class ValPacket[T <: AnyVal@specialized(Primitives)](value: T) extends Packet