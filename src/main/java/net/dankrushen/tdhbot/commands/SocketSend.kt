package net.dankrushen.tdhbot.commands

import net.dankrushen.tdhbot.BotUtils
import net.dankrushen.tdhbot.TDHBot
import net.dankrushen.tdhbot.networking.networkmessage.NetworkRequest
import net.dankrushen.tdhbot.timedobject.TimedObject

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
            for (i in 0 until tdhBot.clients.size) {
                cmdEvent.reply("Sending request to client #${i + 1}...")
                val client = tdhBot.clients[i]

                val response = client.sendRequestBlocking(TimedObject(NetworkRequest(client.generateMessage(args.joinToString(" "))), BotUtils.networkRequestTimeout, BotUtils.networkRequestTimeoutUnit))

                if (response != null)
                    cmdEvent.reply("Response #${i + 1} (${response.id}): ${response.content}")
                else
                    cmdEvent.replyError("Request timed out...")
            }

            cmdEvent.replySuccess("Sent request to clients!")
        }
    }
}