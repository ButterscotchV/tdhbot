package net.dankrushen.tdhbot

import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.CommandClientBuilder
import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import net.dankrushen.tdhbot.commands.*
import net.dankrushen.tdhbot.connector.AccountConnector
import net.dankrushen.tdhbot.connector.ConnectRequest
import net.dankrushen.tdhbot.database.TDHDatabase
import net.dankrushen.tdhbot.networking.ClientController
import net.dankrushen.tdhbot.networking.IClientConnectListener
import net.dankrushen.tdhbot.networking.IClientControllerListener
import net.dankrushen.tdhbot.networking.SocketController
import net.dankrushen.tdhbot.networking.networkmessages.NetworkMessage
import net.dankrushen.tdhbot.networking.networkmessages.NetworkRequest
import net.dankrushen.tdhbot.networking.networkmessages.NetworkResponse
import net.dankrushen.tdhbot.timedobject.TimedObject
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import org.joda.time.DateTime
import java.net.Socket
import java.net.SocketException
import java.nio.file.Paths

class TDHBot : IClientConnectListener, IClientControllerListener {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BotUtils.tokenFile.createNewFile()

            val tdhBot = TDHBot(BotUtils.tokenFile.readText())
            tdhBot.execute()
        }
    }

    val defaultPort = 41242
    var socketController: SocketController = SocketController(this)

    val clients = mutableListOf<ClientController>()

    val tdhDatabase = TDHDatabase(Paths.get("user-database"))

    val accountConnector = AccountConnector(tdhDatabase)

    var client: JDA? = null
    var clientBuilder: JDABuilder
    val commandClient: CommandClient

    constructor(token: String) {
        val builder = CommandClientBuilder()

        builder.setPrefix(".")
        builder.setAlternativePrefix(",")

        builder.addCommand(Ping(this))

        builder.addCommand(ConnectAccount(this))
        builder.addCommand(DisconnectAccount(this))

        builder.addCommand(ManualConnectAccount(this))
        builder.addCommand(ManualFinishConnection(this))
        builder.addCommand(ManualDisconnectAccount(this))

        builder.addCommand(PrintArgs(this))
        builder.addCommand(ListAccounts(this))
        builder.addCommand(SocketSend(this))

        builder.setEmojis(BotUtils.successEmoji, BotUtils.warningEmoji, BotUtils.errorEmoji)

        builder.setOwnerId("145624116932771840")

        commandClient = builder.build()

        clientBuilder = JDABuilder(AccountType.BOT)
                .setAudioEnabled(false)
                .setAutoReconnect(true)
                .setMaxReconnectDelay(32)
                .setToken(token)
                .addEventListener(commandClient) // Add the new CommandClient as a listener
    }

    fun execute(port: Int = defaultPort) {
        println("Running socket...")
        socketController.run(port)

        println("Starting bot...")
        client = clientBuilder.build()
    }

    override fun onClientConnect(client: Socket) {
        println("Client connected! (${client.inetAddress.hostAddress ?: "null"})")

        clients.add(ClientController(client, this))
    }

    override fun onClientDisconnect(controller: ClientController) {
        println("Client disconnected! (${controller.socket.inetAddress.hostAddress ?: "null"})")

        controller.close()
        clients.remove(controller)
    }

    override fun onClientMessage(controller: ClientController, message: NetworkMessage) {
        // println("Message (${message.id}): \"${message.content}\"")
    }

    override fun onClientRequest(controller: ClientController, request: NetworkRequest): NetworkResponse {
        // println("Request (${request.id}): \"${request.content}\"")

        var response = "Success"
        val args = CommandUtils.stringToArgs(request.content)

        if (args.size >= 3) {
            val command = args[0]
            val steamId = args[1]
            val source = args[2]

            when (command.toUpperCase()) {
                "CONNECT" -> {
                    val requestTime = DateTime.now()
                    var connectRequest = accountConnector.getRequest(steamId)

                    if (connectRequest != null) {
                        connectRequest.startDateTime = requestTime

                        sendConsoleMessage(controller, steamId, "You already have a pending connection request\n" +
                                "Your key will expire in ${connectRequest.getTimeToExpiration(requestTime)} second(s)...")
                    } else {
                        connectRequest = accountConnector.addConnectRequest(TimedObject(
                                ConnectRequest(steamId = steamId, requestKey = accountConnector.generateConnectKey()),
                                BotUtils.connectRequestTimeout, BotUtils.connectRequestTimeoutUnit, requestTime,
                                onFinish = { result -> sendConsoleMessage(controller, steamId, if (result.obj.returnedUser == null) "Your accounts are already connected, to re-connect your accounts, you must disconnect them first." else "Your accounts have successfully been connected!") },
                                onExpire = { sendConsoleMessage(controller, steamId, "Your account connection key has expired...") }
                        ))

                        if (connectRequest != null) {
                            sendConsoleMessage(controller, steamId, "Created connection request\n" +
                                    "Your key will expire in ${connectRequest.getTimeToExpiration(requestTime)} second(s)...")
                        } else {
                            sendConsoleMessage(controller, steamId,
                                    "Your accounts are already connected, to re-connect your accounts, you must disconnect them first.")
                        }
                    }

                    if (connectRequest != null)
                        sendConsoleMessage(controller, steamId, "Your connection key is ${connectRequest.obj.requestKey}.")
                }

                "FINISHCONNECT" -> {
                    if (args.size >= 4) {
                        try {
                            val connectKey = args[3].toInt()
                            val connectRequest = accountConnector.getRequest(connectKey)

                            if (connectRequest != null) {
                                if (connectRequest.obj.steamId.isNullOrBlank()) {
                                    connectRequest.obj.steamId = steamId

                                    if (connectRequest.obj.isFilled()) {
                                        accountConnector.completeConnection(connectRequest)
                                        sendConsoleMessage(controller, steamId, "Your accounts have successfully been connected!")
                                    } else {
                                        sendConsoleMessage(controller, steamId, "Something went wrong, please try entering this key in-game or generate a new connection key.")
                                    }
                                } else {
                                    sendConsoleMessage(controller, steamId, "This connection key was already used on Steam, please enter this key on Discord to connect your accounts.")
                                }
                            } else {
                                sendConsoleMessage(controller, steamId, "Invalid connection key, please enter a valid connection key or generate a new connection key.")
                            }
                        } catch (numberFormatException: NumberFormatException) {
                            sendConsoleMessage(controller, steamId, "Invalid connection key, please enter a valid connection key.")
                        }
                    }
                }

                "DISCONNECT" -> {
                    ThreadLocalStoreContainer.transactional(tdhDatabase.xodusStore) {
                        val user = tdhDatabase.getUserDiscordOrSteam(steamId = steamId)

                        if (user != null) {
                            user.delete()
                            sendConsoleMessage(controller, steamId, "Your accounts have successfully been disconnected.")
                        } else {
                            sendConsoleMessage(controller, steamId, "Your account could not be found.")
                        }
                    }
                }

                "GETDISCORDROLES" -> {
                    ThreadLocalStoreContainer.transactional(tdhDatabase.xodusStore) {
                        val user = tdhDatabase.getUserDiscordOrSteam(steamId = steamId)
                        val roles = mutableListOf<String>()

                        if (user != null) {
                            val discordMember = client?.getGuildById(BotUtils.targetGuildId)?.getMemberById(user.discordId)

                            if (discordMember != null) {
                                for (role in discordMember.roles) {
                                    roles.add(role.id)
                                }

                                user.setRoles(roles.toTypedArray())
                            } else {
                                println("Unable to fetch member \"$steamId\"...")

                                roles.addAll(user.getRoles())
                            }
                        }

                        response = roles.joinToString(",")
                    }
                }

                "GETCUSTOMTAG" -> {
                    ThreadLocalStoreContainer.transactional(tdhDatabase.xodusStore, readonly = true) {
                        val user = tdhDatabase.getUserDiscordOrSteam(steamId = steamId)

                        response = user?.customTag ?: ""
                    }
                }
            }
        }

        return NetworkResponse(request.id, response)
    }

    override fun onClientResponse(controller: ClientController, response: NetworkResponse) {
        // println("Response (${response.id}): \"${response.content}\"")

        val request = controller.getRequest(response.id)

        if (request != null) {
            controller.completeRequest(request, response)
        }
    }

    fun sendClientRequest(controller: ClientController, message: String): NetworkResponse? {
        try {
            return controller.sendRequestBlocking(TimedObject(controller.generateRequest(message), BotUtils.networkRequestTimeout, BotUtils.networkRequestTimeoutUnit))
        } catch (socketException: SocketException) {
        }

        return null
    }

    fun sendConsoleMessage(controller: ClientController, steamId: String, message: String): NetworkResponse? {
        return sendClientRequest(controller, "PRINT $steamId \"${message.replace("\n", "{%newline%}")}\"")
    }
}