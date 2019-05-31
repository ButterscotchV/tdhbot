package net.dankrushen.tdhbot

import java.io.File

object BotUtils {
    val tokenFile = File("token.txt")

    const val processingEmoji = "⚙"

    const val successEmoji = "✅"
    const val warningEmoji = "⚠"
    const val errorEmoji = "❌"

    var requestTimeout: Long = 5000
    var requestCheckSpeedMillis: Long = 1000
}