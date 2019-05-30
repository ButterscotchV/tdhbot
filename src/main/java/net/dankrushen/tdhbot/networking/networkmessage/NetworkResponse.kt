package net.dankrushen.tdhbot.networking.networkmessage

class NetworkResponse(id: String, content: String) : NetworkMessage(id, content) {

    companion object {
        val indicator = 'R'
    }

    constructor(networkMessage: NetworkMessage) : this(networkMessage.id, networkMessage.content)

    override fun toString(): String {
        if (content.isBlank())
            return id

        return "$indicator$id:$content"
    }
}