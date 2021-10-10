package fr.overrride.scs.common.packet

/**
 * @param obj the object to store
 * @tparam T the type of the stored object
 */
case class ObjectPacket[T <: AnyRef](obj: T) extends Packet