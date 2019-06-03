package net.dankrushen.tdhbot.timedobject

import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class TimedObject<T>(val obj: T, timeout: Long, timeoutUnit: TimeUnit, startDateTime: DateTime = DateTime.now(), var onFinish: ((TimedObject<T>) -> Unit)? = null, var onExpire: ((TimedObject<T>) -> Unit)? = null) {
    var timeoutUnit = timeoutUnit
        set(value) {
            field = value
            timeoutMillis = value.toMillis(timeout)
        }

    var timeout: Long = timeout
        set(value) {
            if (value < 0)
                throw IllegalArgumentException("${::timeout.name} must not be less than 0")

            field = value
            timeoutMillis = timeoutUnit.toMillis(value)
        }

    var timeoutMillis: Long = timeoutUnit.toMillis(timeout)
        private set(value) {
            field = value
            expirationDateTime = startDateTime + value
        }

    var startDateTime = startDateTime
        set(value) {
            field = value
            expirationDateTime = value + timeoutMillis
        }

    var expirationDateTime: DateTime = startDateTime + timeoutMillis
        private set(value) {
            field = value
        }

    var finished: Boolean = false

    fun getTimeoutTime(timeUnit: TimeUnit = TimeUnit.SECONDS): Long {
        return timeUnit.convert(timeoutMillis, TimeUnit.MILLISECONDS)
    }

    fun getTimeToExpiration(dateTime: DateTime = DateTime.now(), timeUnit: TimeUnit = TimeUnit.SECONDS): Long {
        return timeUnit.convert(expirationDateTime.millis - dateTime.millis, TimeUnit.MILLISECONDS)
    }

    fun isExpired(dateTime: DateTime = DateTime.now()): Boolean {
        return expirationDateTime.millis - dateTime.millis <= 0
    }
}