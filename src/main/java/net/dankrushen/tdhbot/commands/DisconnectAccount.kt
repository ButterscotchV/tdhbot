package net.dankrushen.tdhbot.commands

import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import net.dankrushen.tdhbot.TDHBot

class DisconnectAccount(tdhBot: TDHBot) : BaseCommand(tdhBot) {

    init {
        this.category = Categories.accountManagement

        this.name = "disconnectaccount"
        this.aliases = arrayOf("disconnect")
        this.help = "Disconnects your Discord account from your Steam account on the TDH SCP: SL server"

        this.guildOnly = false

        this.setCommand { cmdEvent, args ->
            ThreadLocalStoreContainer.transactional(tdhBot.tdhDatabase.xodusStore) {
                val user = tdhBot.tdhDatabase.getUserDiscordOrSteam(discordId = cmdEvent.author.id)

                if (user != null) {
                    user.delete()
                    cmdEvent.replySuccess("Your accounts have successfully been disconnected.")
                } else {
                    cmdEvent.replyError("Your account could not be found.")
                }
            }
        }
    }
}