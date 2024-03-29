package net.dankrushen.tdhbot.timedobject

import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class TimedObjectManager<T>(checkDelay: Long = 1, checkUnit: TimeUnit = TimeUnit.SECONDS) {

    var checkUnit: TimeUnit = checkUnit
        set(value) {
            field = value
            internalCheckDelay = value.toMillis(checkDelay)
        }

    private var internalCheckDelay: Long = checkUnit.toMillis(checkDelay)
    var checkDelay: Long = checkDelay
        set(value) {
            if (value < 0)
                throw IllegalArgumentException("${::checkDelay.name} must not be less than 0")

            field = value
            internalCheckDelay = checkUnit.toMillis(value)
        }

    val timedObjects = mutableListOf<TimedObject<T>>()

    val checkThread = thread {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(internalCheckDelay)
            } catch (e: InterruptedException) {
                // Ignore interrupt error
            }

            checkAndExpireObjects()
        }
    }

    fun checkAndExpireObjects() {
        synchronized(timedObjects) {
            for (i in timedObjects.size - 1 downTo 0) {
                val timedObject = timedObjects[i]

                if (timedObject.finished) {
                    finishTimedObject(i)
                } else if (timedObject.isExpired()) {
                    timedObjects.removeAt(i)

                    timedObject.onExpire?.invoke(timedObject)
                }
            }
        }
    }

    fun run() {
        checkThread.start()
    }

    fun shutdown() {
        checkThread.interrupt()
        checkAndExpireObjects()
    }

    fun finishTimedObject(timedObjectIndex: Int, executeOnFinish: Boolean = true): TimedObject<T>? {
        var timedObject: TimedObject<T>

        synchronized(timedObjects) {
            if (timedObjectIndex < 0 || timedObjectIndex >= timedObjects.size)
                return null

            timedObject = timedObjects[timedObjectIndex]
            timedObjects.removeAt(timedObjectIndex)
        }

        if (executeOnFinish)
            timedObject.onFinish?.invoke(timedObject)

        return timedObject
    }

    fun finishTimedObject(timedObject: TimedObject<T>, executeOnFinish: Boolean = true): TimedObject<T>? {
        synchronized(timedObjects) {
            val timedObjectIndex = timedObjects.indexOf(timedObject)

            return finishTimedObject(timedObjectIndex, executeOnFinish)
        }
    }
}