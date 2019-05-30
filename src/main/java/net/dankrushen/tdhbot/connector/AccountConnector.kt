package net.dankrushen.tdhbot.connector

import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import net.dankrushen.tdhbot.database.TDHDatabase
import java.security.SecureRandom
import kotlin.concurrent.thread

class AccountConnector(val tdhDatabase: TDHDatabase) {

    val secureRandom = SecureRandom()
    var numOfDigits = 5

    var connectTimeout = 120L
    val connectRequests = mutableListOf<ConnectRequest>()

    var checkSpeedMillis: Long = 1000

    val connectorTimer = thread {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(checkSpeedMillis)
            } catch (e: InterruptedException) {
                // Ignore interrupt error
            }

            for (i in connectRequests.size - 1 downTo 0) {
                val request = connectRequests[i]

                if (request.isExpired(connectTimeout)) {
                    connectRequests.removeAt(i)
                    request.onExpire?.run()
                }
            }
        }
    }

    fun shutdown() {
        connectorTimer.interrupt()
    }

    fun getRequest(connectKey: Int): ConnectRequest? {
        for (request in connectRequests) {
            if (request.requestKey == connectKey)
                return request
        }

        return null
    }

    fun requestsContains(connectKey: Int): Boolean {
        return getRequest(connectKey) != null
    }

    fun getRequest(discordId: String? = null, steamId: String? = null): ConnectRequest? {
        for (request in connectRequests) {
            if (request.discordId == discordId && request.steamId == steamId)
                return request
            else if (request.discordId == discordId && steamId.isNullOrBlank())
                return request
            else if (discordId.isNullOrBlank() && request.steamId == steamId)
                return request
        }

        return null
    }

    private fun generateRandomKey(numOfDigits: Int): Int {
        val min = Math.pow(10.0, numOfDigits - 1.0).toInt()
        return min + secureRandom.nextInt(9 * min)
    }

    fun generateConnectKey(numOfDigits: Int? = null): Int {
        val numDigits = numOfDigits ?: this.numOfDigits

        var num: Int? = null
        while (num == null || requestsContains(num)) {
            num = generateRandomKey(numDigits)
        }

        return num
    }

    fun generateConnectRequest(discordId: String? = null, steamId: String? = null, numOfDigits: Int? = null, onExpire: Runnable? = null, onConnect: Runnable? = null): ConnectRequest? {
        if (ThreadLocalStoreContainer.transactional(tdhDatabase.xodusStore, readonly = true) { tdhDatabase.containsUserDiscordOrSteam(discordId, steamId) })
            return null

        val connectRequest = ConnectRequest(this, discordId, steamId, generateConnectKey(numOfDigits), onExpire = onExpire, onConnect = onConnect)

        connectRequests.add(connectRequest)

        return connectRequest
    }

    fun completeConnection(connection: ConnectRequest) {
        if (!connection.isFilled())
            throw IllegalArgumentException("connection must be fully filled before being completed")

        connectRequests.remove(connection)

        val discordId = connection.discordId ?: throw NullPointerException("connection has null discordId")
        val steamId = connection.steamId ?: throw NullPointerException("connection has null steamId")

        ThreadLocalStoreContainer.transactional(tdhDatabase.xodusStore) {
            tdhDatabase.makeUser(discordId, steamId)
        }

        connection.onConnect?.run()
    }
}