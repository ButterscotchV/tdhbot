package net.dankrushen.tdhbot.timedobject

import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class TimedObject<T>(val obj: T, timeout: Long, timeoutUnit: TimeUnit, startDateTime: DateTime = DateTime.now(), var onFinish: ((TimedObject<T>) -> Unit)? = null, var onExpire: ((TimedObject<T>) -> Unit)? = null) {
    var timeoutUnit = timeoutUnit
        set(value) {
            field = value
            internalTimeout = value.toMillis(timeout)
        }

    private var internalTimeout: Long = timeoutUnit.toMillis(timeout)
        set(value) {
            field = value
            internalExpirationDateTime = startDateTime + value
        }

    var timeout: Long = timeout
        set(value) {
            if (value < 0)
                throw IllegalArgumentException("${::timeout.name} must not be less than 0")

            field = value
            internalTimeout = timeoutUnit.toMillis(value)
        }

    val timeoutMillis: Long
        get() = internalTimeout

    var startDateTime = startDateTime
        set(value) {
            field = value
            internalExpirationDateTime = value + internalTimeout
        }

    private var internalExpirationDateTime: DateTime = DateTime()
    val expirationDateTime: DateTime
        get() = internalExpirationDateTime

    fun timeToExpiration(dateTime: DateTime = DateTime.now(), timeUnit: TimeUnit = TimeUnit.SECONDS): Long {
        return timeUnit.convert(dateTime.millis - expirationDateTime.millis, TimeUnit.MILLISECONDS)
    }

    fun isExpired(dateTime: DateTime = DateTime.now()): Boolean {
        return dateTime.millis - expirationDateTime.millis <= 0
    }
}