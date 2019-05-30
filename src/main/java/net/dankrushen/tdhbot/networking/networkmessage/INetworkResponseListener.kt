package net.dankrushen.tdhbot.networking.networkmessage

interface INetworkResponseListener {
    fun onNetworkResponse(request: NetworkRequest, response: NetworkResponse)
}