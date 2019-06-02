package net.dankrushen.tdhbot.networking.networkmessages

class NetworkRequest(id: Int, content: String, responseListener: INetworkResponseListener? = null) : NetworkMessage(id, content) {

    constructor(networkMessage: NetworkMessage, responseListener: INetworkResponseListener? = null) : this(networkMessage.id, networkMessage.content, responseListener)

    init {
        if (responseListener != null)
            addResponseListener(responseListener)
    }

    override val messageType = NetworkMessageType.REQUEST

    private val responseListeners = mutableListOf<INetworkResponseListener>()

    fun addResponseListener(responseListener: INetworkResponseListener) {
        responseListeners.add(responseListener)
    }

    fun removeResponseListener(responseListener: INetworkResponseListener) {
        responseListeners.remove(responseListener)
    }

    fun removeAllResponseListeners() {
        responseListeners.clear()
    }

    fun executeResponseHandler(response: NetworkResponse) {
        for (responseListener in responseListeners) {
            responseListener.onNetworkResponse(this, response)
        }
    }
}