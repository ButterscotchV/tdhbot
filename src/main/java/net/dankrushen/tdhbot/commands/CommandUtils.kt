package net.dankrushen.tdhbot.commands

import com.jagrosh.jdautilities.command.CommandEvent
import java.io.PrintWriter
import java.io.StringWriter

object CommandUtils {
    fun exceptionToString(exception: Exception): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)

        exception.printStackTrace(printWriter)

        val stringException = stringWriter.toString()

        printWriter.close()
        stringWriter.close()

        return stringException
    }

    fun indexOfNonEscaped(string: String, char: Char, escapeChar: Char = '\\', startIndex: Int = 0): Int {
        if (string.isNotEmpty()) {
            var escaped = false

            for (i in startIndex until string.length) {
                val stringChar = string[i]

                if (!escaped) {
                    if (stringChar == escapeChar) {
                        escaped = true
                        continue
                    }
                }

                // If the character is escaped or the character that's escaped is an escape character then check if it matches
                if ((!escaped || stringChar == escapeChar) && stringChar == char) {
                    return i
                }

                escaped = false
            }
        }

        return -1
    }

    fun stringToArgs(string: String, separator: Char = ' ', escapeChar: Char = '\\', quoteChar: Char = '"', keepQuotes: Boolean = false): Array<String> {
        if (string.isEmpty())
            return emptyArray()

        val args = mutableListOf<String>()
        val strBuilder = StringBuilder()
        var inQuotes = false
        var escaped = false

        for (i in 0 until string.length) {
            val stringChar = string[i]

            if (!escaped) {
                if (stringChar == escapeChar) {
                    escaped = true
                    continue

                } else if (stringChar == quoteChar && (inQuotes || indexOfNonEscaped(string, quoteChar, escapeChar, i + 1) >= 0)) {
                    // Ignore quotes if there's no future non-escaped quotes

                    inQuotes = !inQuotes
                    if (!keepQuotes)
                        continue

                } else if (!inQuotes && stringChar == separator) {
                    args.add(strBuilder.toString())
                    strBuilder.clear()
                    continue
                }
            }

            strBuilder.append(stringChar)
            escaped = false
        }

        args.add(strBuilder.toString())

        return args.toTypedArray()
    }

    fun removeReaction(cmdEvent: CommandEvent, reactionName: String) {
        cmdEvent.channel.getMessageById(cmdEvent.message.id).queue { reactionMessage ->
            for (reaction in reactionMessage.reactions) {
                if (reaction.isSelf && reaction.reactionEmote.name == reactionName) {
                    reaction.removeReaction().queue()
                }
            }
        }
    }
}