package net.dankrushen.tdhbot.commands

import net.dankrushen.tdhbot.BotUtils
import net.dankrushen.tdhbot.TDHBot
import net.dankrushen.tdhbot.connector.ConnectRequest
import net.dankrushen.tdhbot.timedobject.TimedObject
import org.joda.time.DateTime

class ConnectAccount(tdhBot: TDHBot) : BaseCommand(tdhBot) {

    init {
        this.category = Categories.accountManagement

        this.name = "connectaccount"
        this.aliases = arrayOf("connect")
        this.help = "Begins the process of connecting your Discord account to your Steam account for the TDH SCP: SL server"

        this.arguments = "[Connection Key]"

        this.guildOnly = false

        this.setCommand { cmdEvent, args ->
            val requestTime = DateTime.now()
            var request = tdhBot.accountConnector.getRequest(cmdEvent.author.id)

            if (request != null) {
                request.startDateTime = requestTime

                cmdEvent.replyWarning("You already have a pending connection request, check your DMs for the connection key\n" +
                        "Your key will expire in ${request.getTimeToExpiration(requestTime)} second(s)...")
            } else {
                request = tdhBot.accountConnector.addConnectRequest(TimedObject(
                        ConnectRequest(discordId = cmdEvent.author.id, requestKey = tdhBot.accountConnector.generateConnectKey()),
                        BotUtils.connectRequestTimeout, BotUtils.connectRequestTimeoutUnit, requestTime,
                        onFinish = { result -> if (result.obj.returnedUser == null) cmdEvent.replyError("Your accounts are already connected, to re-connect your accounts, you must disconnect them first.") else cmdEvent.replyInDm("Your accounts have successfully been connected!") },
                        onExpire = { cmdEvent.replyInDm("Your account connection key has expired...") }
                ))

                if (request != null) {
                    cmdEvent.reply("Created connection request, check your DMs for the connection key\n" +
                            "Your key will expire in ${request.getTimeToExpiration(requestTime)} second(s)...")
                } else {
                    cmdEvent.replyError("Your accounts are already connected, to re-connect your accounts, you must disconnect them first.")
                }
            }

            if (request != null)
                cmdEvent.replyInDm("Your connection key is `${request.obj.requestKey}`.")
        }

        this.setCommand(1) { cmdEvent, args ->
            try {
                val connectKey = args[0].toInt()
                val request = tdhBot.accountConnector.getRequest(connectKey)

                if (request != null) {
                    if (request.obj.discordId.isNullOrBlank()) {
                        request.obj.discordId = cmdEvent.author.id

                        if (request.obj.isFilled()) {
                            tdhBot.accountConnector.completeConnection(request)
                            cmdEvent.replySuccess("Your accounts have successfully been connected!")
                        } else {
                            cmdEvent.replyError("Something went wrong, please try entering this key in-game or generate a new connection key.")
                        }
                    } else {
                        cmdEvent.replyError("This connection key was already used on Discord, please enter this key in-game to connect your accounts.")
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