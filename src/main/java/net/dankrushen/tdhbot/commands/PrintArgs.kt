package net.dankrushen.tdhbot.commands

import net.dankrushen.tdhbot.TDHBot

class PrintArgs(tdhBot: TDHBot) : BaseCommand(tdhBot) {

    init {
        this.category = Categories.dev

        this.name = "printarguments"
        this.aliases = arrayOf("printargs")
        this.help = "Prints out the provided arguments"

        this.arguments = "<Arguments...>"

        this.guildOnly = false

        this.setCommand(1) { cmdEvent, args ->
            cmdEvent.reply(args.joinToString("\", \"", "[\"", "\"]"))
        }
    }
}