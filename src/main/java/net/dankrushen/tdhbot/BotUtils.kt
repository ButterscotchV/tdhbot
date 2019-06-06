package net.dankrushen.tdhbot

import java.io.File
import java.util.concurrent.TimeUnit

object BotUtils {
    val tokenFile = File("token.txt")

    const val targetGuildId = "433794474288873472"

    const val processingEmoji = "⚙"

    const val successEmoji = "✅"
    const val warningEmoji = "⚠"
    const val errorEmoji = "❌"

    var connectRequestTimeout: Long = 2
    var connectRequestTimeoutUnit: TimeUnit = TimeUnit.MINUTES

    var networkRequestTimeout: Long = 10
    var networkRequestTimeoutUnit: TimeUnit = TimeUnit.SECONDS
}