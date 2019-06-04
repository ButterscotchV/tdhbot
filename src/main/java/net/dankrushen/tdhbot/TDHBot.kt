package net.dankrushen.tdhbot

import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.CommandClientBuilder
import net.dankrushen.tdhbot.commands.*
import net.dankrushen.tdhbot.connector.AccountConnector
import net.dankrushen.tdhbot.database.TDHDatabase
import net.dankrushen.tdhbot.networking.ClientController
import net.dankrushen.tdhbot.networking.IClientConnectListener
import net.dankrushen.tdhbot.networking.IClientControllerListener
import net.dankrushen.tdhbot.networking.SocketController
import net.dankrushen.tdhbot.networking.networkmessages.NetworkMessage
import net.dankrushen.tdhbot.networking.networkmessages.NetworkRequest
import net.dankrushen.tdhbot.networking.networkmessages.NetworkResponse
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import java.net.Socket
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
        println("Message (${message.id}): \"${message.content}\"")
    }

    override fun onClientRequest(controller: ClientController, request: NetworkRequest): NetworkResponse {
        println("Request (${request.id}): \"${request.content}\"")

        return NetworkResponse(request.id, "Testing from TDHBot to TDHPlugin")
    }

    override fun onClientResponse(controller: ClientController, response: NetworkResponse) {
        println("Response (${response.id}): \"${response.content}\"")

        val request = controller.getRequest(response.id)

        if (request != null) {
            controller.completeRequest(request, response)
        }
    }
}