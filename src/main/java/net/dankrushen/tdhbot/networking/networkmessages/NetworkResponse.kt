package net.dankrushen.tdhbot.networking.networkmessages

class NetworkResponse(id: Int, content: String) : NetworkMessage(id, content) {

    constructor(networkMessage: NetworkMessage) : this(networkMessage.id, networkMessage.content)

    override val messageType = NetworkMessageType.RESPONSE
}