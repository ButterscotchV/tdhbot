package net.dankrushen.tdhbot.commands

import net.dankrushen.tdhbot.TDHBot

class ManualFinishConnection(tdhBot: TDHBot) : BaseCommand(tdhBot) {

    init {
        this.category = Categories.admin

        this.name = "manualfinishconnect"
        this.aliases = arrayOf("manualfinish")
        this.help = "Finishes the connection of a Discord account to a Steam account for the TDH SCP: SL server"

        this.arguments = "<Connection Key> <Steam ID>"

        this.guildOnly = true

        this.setCommand(2) { cmdEvent, args ->
            try {
                val connectKey = args[0].toInt()
                val request = tdhBot.accountConnector.getRequest(connectKey)

                if (request != null) {
                    if (request.steamId.isNullOrBlank()) {
                        request.steamId = args[1]

                        if (request.isFilled()) {
                            tdhBot.accountConnector.completeConnection(request)
                            cmdEvent.replySuccess("Successfully connected accounts!")
                        } else {
                            cmdEvent.replyError("Something went wrong, please try entering this key in-game or generate a new connection key.")
                        }
                    } else {
                        cmdEvent.replyError("This connection key was already used on Steam, please enter this key on Discord to connect accounts.")
                    }
                } else {
                    cmdEvent.replyError("Invalid connection key, please enter a valid connection key or generate a new connection key.")
                }
            } catch (numberFormatException: NumberFormatException) {
                cmdEvent.replyError("Invalid connection key, please enter a valid connection key.")
            }
        }
    }
}