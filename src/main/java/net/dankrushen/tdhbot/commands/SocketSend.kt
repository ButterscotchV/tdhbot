package net.dankrushen.tdhbot.commands

import com.google.common.graph.Network
import net.dankrushen.tdhbot.TDHBot
import net.dankrushen.tdhbot.networking.networkmessage.NetworkMessage
import net.dankrushen.tdhbot.networking.networkmessage.NetworkRequest

class SocketSend(tdhBot: TDHBot) : BaseCommand(tdhBot) {

    init {
        this.category = Categories.dev

        this.name = "socketsend"
        this.help = "Sends a message through the bot's socket to the SCP: SL server"

        this.guildOnly = false

        this.setCommand { cmdEvent, args ->
            val builder = StringBuilder()

            for (i in 0 until 10000) {
                builder.append('F')
            }

            for (client in tdhBot.clients) {
                client.sendMessage(client.generateMessage(builder.toString()))
            }

            cmdEvent.replySuccess("Sent message to clients!")
        }

        this.setCommand(1) { cmdEvent, args ->
            for (client in tdhBot.clients) {
                client.sendMessage(client.generateMessage(args.joinToString(" ")))
            }

            cmdEvent.replySuccess("Sent message to clients!")
        }
    }
}