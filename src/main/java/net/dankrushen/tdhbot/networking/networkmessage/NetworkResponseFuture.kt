package net.dankrushen.tdhbot.networking.networkmessage

import java.util.concurrent.CompletableFuture

class NetworkResponseFuture : CompletableFuture<NetworkResponse>(), INetworkResponseListener {
    override fun onNetworkResponse(request: NetworkRequest, response: NetworkResponse) {
        complete(response)
    }

    override fun onNetworkRequestTimeout(request: NetworkRequest) {
        cancel(true)
    }
}