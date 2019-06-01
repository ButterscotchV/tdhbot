package net.dankrushen.tdhbot.timedobject

import java.util.concurrent.TimeUnit

class TimedObjectManager<T>(checkDelay: Long = 1, checkUnit: TimeUnit = TimeUnit.SECONDS) {

    var checkUnit: TimeUnit = checkUnit
        set(value) {
            field = value
            internalCheckDelay = value.toMillis(checkDelay)
        }

    private var internalCheckDelay: Long = -1
    var checkDelay: Long = checkDelay
        set(value) {
            if (value < 0)
                throw IllegalArgumentException("${::checkDelay.name} must not be less than 0")

            field = value
            internalCheckDelay = checkUnit.toMillis(value)
        }

    val timedObjects = mutableListOf<TimedObject<T>>()

    val checkThread = Thread {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(internalCheckDelay)
            } catch (e: InterruptedException) {
                // Ignore interrupt error
            }

            for (i in timedObjects.size - 1 downTo 0) {
                val timedObject = timedObjects[i]

                if (timedObject.isExpired()) {
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
    }

    fun finishTimedObject(timedObject: TimedObject<T>): T? {
        return if (timedObject.finishedCheck?.invoke(timedObject) != false) {
            timedObjects.remove(timedObject)

            timedObject.onFinish?.invoke(timedObject)

            timedObject.obj
        } else {
            null
        }
    }
}