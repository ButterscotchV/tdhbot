package net.dankrushen.tdhbot.networking.networkmessage

import net.dankrushen.tdhbot.BotUtils
import org.joda.time.DateTime
import org.joda.time.Duration

class TimedNetworkRequest(val request: NetworkRequest, var startTime: DateTime = DateTime.now(), var onSuccess: ((NetworkResponse) -> Unit)? = null, var onExpire: (() -> Unit)? = null, var onError: ((String) -> Unit)? = null) {
    fun resetStartTime(startTime: DateTime = DateTime.now()) {
        this.startTime = startTime
    }

    fun secondsToExpiration(timeoutSeconds: Long = BotUtils.requestTimeout): Long {
        return Math.max(timeoutSeconds - Duration(startTime, DateTime.now()).standardSeconds, 0)
    }

    fun isExpired(timeoutSeconds: Long = BotUtils.requestTimeout): Boolean {
        return Duration(startTime, DateTime.now()).standardSeconds >= timeoutSeconds
    }
}