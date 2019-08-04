package net.dankrushen.tdhbot.networking

import net.dankrushen.tdhbot.networking.networkmessages.*
import net.dankrushen.tdhbot.timedobject.TimedObject
import net.dankrushen.tdhbot.timedobject.TimedObjectManager
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class ClientController(val socket: Socket, var requestListener: IClientControllerListener) : Closeable {
    companion object {
        fun waitOnRequest(requestWaitable: NetworkResponseFuture, timeout: Long = -1, timeoutUnit: TimeUnit = TimeUnit.SECONDS): NetworkResponse? {
            return try {
                if (timeout >= 0)
                    requestWaitable.get(timeout, timeoutUnit)
                else
                    requestWaitable.get()
            } catch (cancelledException: CancellationException) {
                null
            } catch (timeoutException: TimeoutException) {
                null
            } catch (interruptedException: InterruptedException) {
                null
            }
        }
    }

    private var listenerExecutor = Executors.newCachedThreadPool()

    private val writeLock = ReentrantLock()

    private val inputStream = DataInputStream(socket.getInputStream())
    private val writer = DataOutputStream(socket.getOutputStream())

    val timedRequestManager = TimedObjectManager<NetworkRequest>()

    private var networkId = Int.MIN_VALUE

    private var closed: Boolean = false

    private var clientThread = thread {
        while (!closed && socket.isConnected) {
            try {
                val id = inputStream.readInt()

                val typeId = try {
                    NetworkMessageType.fromId(inputStream.readInt())
                } catch (illegalArgumentException: IllegalArgumentException) {
                    println("Error in received message: Invalid message type ID")
                    continue
                }

                val length = inputStream.readInt()
                if (length < 0) {
                    println("Error in received message: Invalid message length")
                    continue
                }

                val message = if (length == 0) "" else String(inputStream.readNBytes(length), Charsets.UTF_8)

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
            } catch (eofException: EOFException) {
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

    override fun close() {
        socket.close()
        listenerExecutor.shutdown()
        closed = true
    }

    fun write(id: Int, message: String, type: NetworkMessageType = NetworkMessageType.MESSAGE) {
        writeLock.withLock {
            writer.writeInt(id)
            writer.writeInt(type.id)

            val messageBytes = message.toByteArray(Charsets.UTF_8)

            writer.writeInt(messageBytes.size)

            if (messageBytes.isNotEmpty())
                writer.write(messageBytes)
        }
    }

    fun generateMessage(content: String): NetworkMessage {
        return NetworkMessage(networkId++, content)
    }

    fun generateRequest(content: String, responseListener: INetworkResponseListener? = null): NetworkRequest {
        return NetworkRequest(networkId++, content, responseListener)
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

    fun sendRequestBlocking(request: TimedObject<NetworkRequest>, additionalTimeout: Long = 1, additionalTimeoutUnit: TimeUnit = TimeUnit.SECONDS): NetworkResponse? {
        return if (additionalTimeout >= 0) {
            val timeout = request.timeoutMillis + additionalTimeoutUnit.toMillis(additionalTimeout)
            waitOnRequest(sendRequestWaitable(request), timeout, TimeUnit.MILLISECONDS)
        } else {
            waitOnRequest(sendRequestWaitable(request), request.timeout, request.timeoutUnit)
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