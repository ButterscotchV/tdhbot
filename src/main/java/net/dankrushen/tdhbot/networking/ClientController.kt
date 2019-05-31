package net.dankrushen.tdhbot.networking

import net.dankrushen.tdhbot.networking.networkmessage.NetworkMessage
import net.dankrushen.tdhbot.networking.networkmessage.NetworkRequest
import net.dankrushen.tdhbot.networking.networkmessage.NetworkResponse
import java.io.OutputStream
import java.net.Socket
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ClientController(val socket: Socket, var requestListener: IClientControllerListener) {

    private var listenerExecutor = Executors.newCachedThreadPool()

    private val reader: Scanner = Scanner(socket.getInputStream())
    private val writer: OutputStream = socket.getOutputStream()

    private var networkId = 0

    private var clientThread = thread {
        while (!Thread.interrupted() && socket.isConnected) {
            try {
                val message = reader.nextLine()

                if (!message.contains(':')) {
                    println("Error in received message: Does not contain separator")
                    continue
                }

                val splitMessage = message.split(Regex.fromLiteral(":"), 2)

                if (splitMessage.size != 2) {
                    println("Error in received message: Size does not match")
                    continue
                }

                val id = splitMessage[0]

                if (id.isBlank()) {
                    println("Error in received message: id is blank")
                    continue
                }

                val content = splitMessage[1]

                if (id[0] == NetworkRequest.indicator) {
                    if (id.length <= 1) {
                        println("Error in received message: NetworkRequest id is blank")
                        continue
                    }

                    listenerExecutor.submit {
                        sendMessage(requestListener.onClientRequest(this, NetworkRequest(id.substring(1), content)))
                    }
                } else if (id[0] == NetworkResponse.indicator) {
                    if (id.length <= 1) {
                        println("Error in received message: NetworkResponse id is blank")
                        continue
                    }

                    listenerExecutor.submit {
                        requestListener.onClientResponse(this, NetworkResponse(id.substring(1), content))
                    }
                } else {
                    listenerExecutor.submit {
                        requestListener.onClientMessage(this, NetworkMessage(id, content))
                    }
                }
            } catch (noSuchElementException: NoSuchElementException) {
                close()
                requestListener.onClientDisconnect(this)
            } catch (e: Exception) {
                println("Error in ClientController:")
                e.printStackTrace()
            }
        }
    }

    fun close() {
        clientThread.interrupt()
        socket.close()
        listenerExecutor.shutdown()
    }

    fun write(message: String) {
        writer.write(("$message\n").toByteArray())
    }

    fun generateMessage(content: String): NetworkMessage {
        return NetworkMessage(networkId++.toString(), content)
    }

    fun sendMessage(message: NetworkMessage) {
        write(message.toString())
    }
}