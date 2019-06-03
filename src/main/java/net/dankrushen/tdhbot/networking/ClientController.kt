package net.dankrushen.tdhbot.networking

import net.dankrushen.tdhbot.networking.networkmessages.*
import net.dankrushen.tdhbot.timedobject.TimedObject
import net.dankrushen.tdhbot.timedobject.TimedObjectManager
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class ClientController(val socket: Socket, var requestListener: IClientControllerListener) {

    private var listenerExecutor = Executors.newCachedThreadPool()

    private val writeLock = ReentrantLock()

    private val inputStream = socket.getInputStream()
    private val reader = Scanner(inputStream)
    private val writer = socket.getOutputStream()

    val intBuffer = ByteArray(4)

    val timedRequestManager = TimedObjectManager<NetworkRequest>()

    private var networkId = Int.MIN_VALUE

    private var clientThread = thread {
        while (!Thread.interrupted() && socket.isConnected) {
            try {
                val id = inputStream.readInt(intBuffer)

                val typeId = try {
                    NetworkMessageType.fromId(inputStream.readInt(intBuffer))
                } catch (illegalArgumentException: IllegalArgumentException) {
                    println("Error in received message: Invalid message type ID")
                    continue
                }

                val message = reader.nextLine()

                when (typeId) {
                    NetworkMessageType.MESSAGE -> listenerExecutor.submit {
                        requestListener.onClientMessage(this, NetworkMessage(id, message))
                    }

                    NetworkMessageType.REQUEST -> listenerExecutor.submit {
                        sendMessage(requestListener.onClientRequest(this, NetworkRequest(id, message)))
                    }

                    NetworkMessageType.RESPONSE -> listenerExecutor.submit {
                        requestListener.onClientResponse(this, NetworkResponse(id, message))
                    }
                }
            } catch (socketException: SocketException) {
                onDisconnect()
            } catch (noSuchElementException: NoSuchElementException) {
                onDisconnect()
            } catch (e: Exception) {
                println("Error in ClientController:")
                e.printStackTrace()
            }
        }
    }

    private fun onDisconnect() {
        close()
        requestListener.onClientDisconnect(this)
    }

    fun close() {
        clientThread.interrupt()
        socket.close()
        listenerExecutor.shutdown()
    }

    fun write(id: Int, message: String, type: NetworkMessageType = NetworkMessageType.MESSAGE) {
        writeLock.withLock {
            val buffer = ByteBuffer.allocate(Int.SIZE_BYTES * 2)

            buffer.putInt(id)
            buffer.putInt(type.id)

            writer.write(buffer.array())
            writer.write(("$message\n").toByteArray())
        }
    }

    fun generateMessage(content: String): NetworkMessage {
        return NetworkMessage(networkId++, content)
    }

    fun sendMessage(message: NetworkMessage) {
        write(message.id, message.content, message.messageType)
    }

    fun sendRequest(request: TimedObject<NetworkRequest>) {
        synchronized(timedRequestManager.timedObjects) {
            timedRequestManager.timedObjects += request
        }

        sendMessage(request.obj)
    }

    fun sendRequestWaitable(request: TimedObject<NetworkRequest>): NetworkResponseFuture {
        val future = NetworkResponseFuture()
        request.obj.addResponseListener(future)

        sendRequest(request)

        return future
    }

    fun sendRequestBlocking(request: TimedObject<NetworkRequest>): NetworkResponse? {
        return try {
            sendRequestWaitable(request).get()
        } catch (cancelledException: CancellationException) {
            null
        }
    }

    fun getRequest(id: Int): TimedObject<NetworkRequest>? {
        synchronized(timedRequestManager.timedObjects) {
            for (request in timedRequestManager.timedObjects) {
                if (request.obj.id == id)
                    return request
            }
        }

        return null
    }

    fun completeRequest(request: TimedObject<NetworkRequest>, response: NetworkResponse) {
        timedRequestManager.finishTimedObject(request)

        request.obj.executeResponseHandler(response)
    }
}