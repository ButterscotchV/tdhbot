package net.dankrushen.tdhbot.networking

import net.dankrushen.tdhbot.networking.networkmessages.NetworkMessage
import net.dankrushen.tdhbot.networking.networkmessages.NetworkRequest
import net.dankrushen.tdhbot.networking.networkmessages.NetworkResponse

interface IClientControllerListener {
    fun onClientDisconnect(controller: ClientController)

    fun onClientMessage(controller: ClientController, message: NetworkMessage)
    fun onClientRequest(controller: ClientController, request: NetworkRequest): NetworkResponse
    fun onClientResponse(controller: ClientController, response: NetworkResponse)
}