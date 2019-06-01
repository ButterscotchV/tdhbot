package net.dankrushen.tdhbot.networking.networkmessage

class NetworkRequest(id: String, content: String, responseListener: INetworkResponseListener? = null) : NetworkMessage(id, content) {

    companion object {
        const val indicator = 'Q'
    }

    constructor(networkMessage: NetworkMessage, responseListener: INetworkResponseListener? = null) : this(networkMessage.id, networkMessage.content, responseListener)

    init {
        if (responseListener != null)
            addResponseListener(responseListener)
    }

    private val responseListeners = mutableListOf<INetworkResponseListener>()

    override fun toString(): String {
        return "$indicator$id:$content"
    }

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