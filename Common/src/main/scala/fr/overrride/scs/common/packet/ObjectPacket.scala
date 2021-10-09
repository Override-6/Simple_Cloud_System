package fr.overrride.scs.common.packet

case class ObjectPacket[T <: AnyRef](obj: T) extends Packet