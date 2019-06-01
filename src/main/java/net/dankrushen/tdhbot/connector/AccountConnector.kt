package net.dankrushen.tdhbot.connector

import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import net.dankrushen.tdhbot.database.TDHDatabase
import net.dankrushen.tdhbot.database.user.XdUser
import net.dankrushen.tdhbot.timedobject.TimedObject
import net.dankrushen.tdhbot.timedobject.TimedObjectManager
import java.security.SecureRandom

class AccountConnector(val tdhDatabase: TDHDatabase) {

    val secureRandom = SecureRandom()
    var numOfDigits = 5

    val timedRequestManager = TimedObjectManager<ConnectRequest>()

    fun getRequest(connectKey: Int): TimedObject<ConnectRequest>? {
        for (request in timedRequestManager.timedObjects) {
            if (request.obj.requestKey == connectKey)
                return request
        }

        return null
    }

    fun requestsContains(connectKey: Int): Boolean {
        return getRequest(connectKey) != null
    }

    fun getRequest(discordId: String? = null, steamId: String? = null): TimedObject<ConnectRequest>? {
        for (request in timedRequestManager.timedObjects) {
            if (request.obj.discordId == discordId && request.obj.steamId == steamId)
                return request
            else if (request.obj.discordId == discordId && steamId.isNullOrBlank())
                return request
            else if (discordId.isNullOrBlank() && request.obj.steamId == steamId)
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

    fun addConnectRequest(connectRequest: TimedObject<ConnectRequest>): TimedObject<ConnectRequest>? {
        if (ThreadLocalStoreContainer.transactional(tdhDatabase.xodusStore, readonly = true) { tdhDatabase.containsUserDiscordOrSteam(connectRequest.obj.discordId, connectRequest.obj.steamId) })
            return null

        timedRequestManager.timedObjects += connectRequest

        return connectRequest
    }

    fun completeConnection(request: TimedObject<ConnectRequest>): XdUser? {
        if (!request.obj.isFilled())
            throw IllegalArgumentException("request must be fully filled before being completed")

        timedRequestManager.finishTimedObject(request, false)

        val discordId = request.obj.discordId ?: throw NullPointerException("request has null discordId")
        val steamId = request.obj.steamId ?: throw NullPointerException("request has null steamId")

        return ThreadLocalStoreContainer.transactional(tdhDatabase.xodusStore) {
            if (!tdhDatabase.containsUserDiscordOrSteam(discordId, steamId)) {
                val xdUser = tdhDatabase.makeUser(discordId, steamId)

                request.obj.returnedUser = xdUser
                request.onFinish?.invoke(request)

                xdUser
            } else {
                request.onFinish?.invoke(request)

                null
            }
        }
    }
}