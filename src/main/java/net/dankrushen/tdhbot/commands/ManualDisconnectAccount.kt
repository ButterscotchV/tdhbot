package net.dankrushen.tdhbot.commands

import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import net.dankrushen.tdhbot.TDHBot

class ManualDisconnectAccount(tdhBot: TDHBot) : BaseCommand(tdhBot) {

    init {
        this.category = Categories.admin

        this.name = "manualdisconnectaccount"
        this.aliases = arrayOf("manualdisconnect")
        this.help = "Disconnects a Discord and Steam account on the TDH SCP: SL server"

        this.arguments = "<Discord ID / Steam ID>"

        this.guildOnly = true

        this.setCommand(1) { cmdEvent, args ->
            ThreadLocalStoreContainer.transactional(tdhBot.tdhDatabase.xodusStore) {
                val user = tdhBot.tdhDatabase.getUserDiscordOrSteam(args[0])

                if (user != null) {
                    user.delete()
                    cmdEvent.replySuccess("Manually disconnected accounts.")
                } else {
                    cmdEvent.replyError("Account could not be found.")
                }
            }
        }
    }
}