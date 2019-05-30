package net.dankrushen.tdhbot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dankrushen.tdhbot.BotUtils
import net.dankrushen.tdhbot.TDHBot

abstract class BaseCommand(val tdhBot: TDHBot) : Command() {

    private val commands = mutableMapOf<Int, (CommandEvent, Array<String>) -> Unit>()

    override fun execute(cmdEvent: CommandEvent) {
        try {
            cmdEvent.message.addReaction(BotUtils.processingEmoji).complete()

            val args = CommandUtils.stringToArgs(cmdEvent.args)

            val command = getCommand(args.size)

            if (command != null) {
                command(cmdEvent, args)
            } else {
                cmdEvent.replyError("Incorrect number of arguments.")
            }

            CommandUtils.removeReaction(cmdEvent, BotUtils.processingEmoji)
        } catch (e: Exception) {
            e.printStackTrace()
            cmdEvent.reactError()
            cmdEvent.replyError("```\n${CommandUtils.exceptionToString(e)}\n```")
        }
    }

    fun getCommand(numArgs: Int = 0): ((CommandEvent, Array<String>) -> Unit)? {
        // Closest as in closest below numArgs
        var closestEntry: Map.Entry<Int, (CommandEvent, Array<String>) -> Unit>? = null

        for (entry in commands) {
            // If the number of arguments matches exactly, return this immediately
            if (entry.key == numArgs)
                return entry.value

            // For entries with the same or less arguments, find the maximum argument command
            if (entry.key <= numArgs && (closestEntry == null || entry.key > closestEntry.key)) {
                closestEntry = entry
            }
        }

        return closestEntry?.value
    }

    fun setCommand(numArgs: Int = 0, body: (CommandEvent, Array<String>) -> Unit) {
        if (numArgs < 0)
            throw IllegalArgumentException("numArgs must not be less than 0")

        commands[numArgs] = body
    }

    fun removeCommand(numArgs: Int) {
        commands.remove(numArgs)
    }

    fun removeAllCommands() {
        commands.clear()
    }
}