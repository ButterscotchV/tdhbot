package net.dankrushen.tdhbot.networking.networkmessages

interface INetworkResponseListener {
    fun onNetworkResponse(request: NetworkRequest, response: NetworkResponse)
    fun onNetworkRequestTimeout(request: NetworkRequest)
}