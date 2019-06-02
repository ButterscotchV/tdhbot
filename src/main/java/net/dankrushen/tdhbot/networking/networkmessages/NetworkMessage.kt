package net.dankrushen.tdhbot.networking.networkmessages

open class NetworkMessage(val id: Int, val content: String) {

    open val messageType = NetworkMessageType.MESSAGE

    override fun toString(): String {
        return "$id:$messageType:$content"
    }
}