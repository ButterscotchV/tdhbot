package net.dankrushen.tdhbot.networking

import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class SocketController {
    private var socket: ServerSocket? = null

    private val clientListeners = mutableListOf<IClientConnectListener>()
    private var clientListenerExecutor: ExecutorService? = null

    private var serverThread: Thread? = null

    constructor(clientListener: IClientConnectListener? = null) {
        if (clientListener != null)
            addClientListener(clientListener)
    }

    fun run(port: Int) {
        if (socket?.isBound == true)
            throw Exception("Server is already running")

        socket = ServerSocket(port)

        clientListenerExecutor = Executors.newCachedThreadPool()

        serverThread = thread {
            while (!Thread.interrupted() && socket?.isBound == true) {
                var clientSocket: Socket? = null

                try {
                    clientSocket = socket?.accept()
                } catch (socketException: SocketException) {
                    // Ignoring socket exceptions
                }

                if (clientSocket != null) {
                    clientListenerExecutor?.submit {
                        for (clientListener in clientListeners) {
                            clientListener.onClientConnect(clientSocket)
                        }
                    }
                }
            }
        }
    }

    fun close() {
        serverThread?.interrupt()
        socket?.close()
        clientListenerExecutor?.shutdown()
    }

    fun addClientListener(clientListener: IClientConnectListener) {
        clientListeners.add(clientListener)
    }

    fun removeClientListener(clientListener: IClientConnectListener) {
        clientListeners.remove(clientListener)
    }

    fun removeAllClientListeners() {
        clientListeners.clear()
    }

    fun executeListeners(clientSocket: Socket) {
        clientListenerExecutor?.submit {
            for (clientListener in clientListeners) {
                clientListener.onClientConnect(clientSocket)
            }
        }
    }
}