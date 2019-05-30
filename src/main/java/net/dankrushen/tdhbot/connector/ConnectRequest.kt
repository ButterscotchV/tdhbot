package net.dankrushen.tdhbot.connector

import net.dankrushen.glovelib.database.keyvector.XdUser
import org.joda.time.DateTime
import org.joda.time.Duration

class ConnectRequest(val accountConnector: AccountConnector, var discordId: String? = null, var steamId: String? = null, val requestKey: Int, var startTime: DateTime = DateTime.now(), var onConnect: ((XdUser) -> Unit)? = null, var onExpire: (() -> Unit)? = null, var onError: ((String) -> Unit)? = null) {

    fun resetStartTime(startTime: DateTime = DateTime.now()) {
        this.startTime = startTime
    }

    fun secondsToExpiration(timeoutSeconds: Long = accountConnector.connectTimeout): Long {
        return Math.max(timeoutSeconds - Duration(startTime, DateTime.now()).standardSeconds, 0)
    }

    fun isExpired(timeoutSeconds: Long = accountConnector.connectTimeout): Boolean {
        return Duration(startTime, DateTime.now()).standardSeconds >= timeoutSeconds
    }

    fun isFilled(): Boolean {
        return !(discordId.isNullOrBlank() || steamId.isNullOrBlank())
    }

    fun isExpiredAndNotFilled(timeoutSeconds: Long): Boolean {
        return isExpired(timeoutSeconds) && !isFilled()
    }
}