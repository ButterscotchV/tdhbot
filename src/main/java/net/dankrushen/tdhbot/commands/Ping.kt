package net.dankrushen.tdhbot.commands

import net.dankrushen.tdhbot.TDHBot

class Ping(tdhBot: TDHBot) : BaseCommand(tdhBot) {

    init {
        this.category = Categories.general

        this.name = "ping"
        this.help = "Outputs the bot's ping"

        this.guildOnly = false

        this.setCommand { cmdEvent, args ->
            cmdEvent.reply("Pong! `${tdhBot.client?.ping} ms`")
        }
    }
}