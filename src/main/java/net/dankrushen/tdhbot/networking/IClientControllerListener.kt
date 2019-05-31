package net.dankrushen.tdhbot.networking

import net.dankrushen.tdhbot.networking.networkmessage.NetworkMessage
import net.dankrushen.tdhbot.networking.networkmessage.NetworkRequest
import net.dankrushen.tdhbot.networking.networkmessage.NetworkResponse

interface IClientControllerListener {
    fun onClientDisconnect(controller: ClientController)

    fun onClientMessage(controller: ClientController, message: NetworkMessage)
    fun onClientRequest(controller: ClientController, request: NetworkRequest): NetworkResponse
    fun onClientResponse(controller: ClientController, response: NetworkResponse)
}