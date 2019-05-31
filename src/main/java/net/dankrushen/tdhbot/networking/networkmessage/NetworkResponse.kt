package net.dankrushen.tdhbot.networking.networkmessage

class NetworkResponse(id: String, content: String) : NetworkMessage(id, content) {

    companion object {
        const val indicator = 'R'
    }

    constructor(networkMessage: NetworkMessage) : this(networkMessage.id, networkMessage.content)

    override fun toString(): String {
        return "$indicator$id:$content"
    }
}