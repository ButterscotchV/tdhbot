package net.dankrushen.tdhbot.commands

import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import net.dankrushen.tdhbot.TDHBot

class ManualConnectAccount(tdhBot: TDHBot) : BaseCommand(tdhBot) {

    init {
        this.category = Categories.admin

        this.name = "manualconnectaccount"
        this.aliases = arrayOf("manualconnect")
        this.help = "Manually connects a Discord account to a Steam account for the TDH SCP: SL server"

        this.arguments = "<Discord ID> <Steam ID>"

        this.guildOnly = true

        this.setCommand(2) { cmdEvent, args ->
            if (ThreadLocalStoreContainer.transactional(tdhBot.tdhDatabase.xodusStore) { tdhBot.tdhDatabase.makeUserOrIfExistsReturnNull(args[0], args[1]) } != null)
                cmdEvent.replySuccess("Manually connected Discord account to Steam account.")
            else
                cmdEvent.replyError("Unable to manually connect Discord account to Steam account, it already exists in the database.")
        }
    }
}