package net.dankrushen.tdhbot.networking.networkmessage

open class NetworkMessage(val id: String, val content: String) {

    init {
        if (id.isBlank())
            throw IllegalArgumentException("id must not be blank")
    }

    override fun toString(): String {
        return "$id:$content"
    }
}