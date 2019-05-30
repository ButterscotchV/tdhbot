package net.dankrushen.tdhbot.networking

import java.net.Socket

interface IClientConnectListener {
    fun onClientConnect(client: Socket)
}