package net.dankrushen.tdhbot.commands

import kotlinx.dnq.query.iterator
import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import net.dankrushen.glovelib.database.keyvector.XdUser
import net.dankrushen.tdhbot.TDHBot

class ListAccounts(tdhBot: TDHBot) : BaseCommand(tdhBot) {

    init {
        this.category = Categories.dev

        this.name = "listaccounts"
        this.aliases = arrayOf("listusers")
        this.help = "Lists all connected user accounts"

        this.guildOnly = false

        this.setCommand { cmdEvent, args ->
            val builder = StringBuilder()

            builder.append("```\nDiscord ID : Steam ID\n")

            ThreadLocalStoreContainer.transactional(tdhBot.tdhDatabase.xodusStore, readonly = true) {
                for (user in XdUser.all().iterator()) {
                    builder.append("${user.discordId} : ${user.steamId}\n")
                }
            }

            builder.append("```")

            cmdEvent.reply(builder.toString())
        }
    }
}