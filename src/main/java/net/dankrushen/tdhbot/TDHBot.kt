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
import net.dankrushen.tdhbot.networking.networkmessage.NetworkMessage
import net.dankrushen.tdhbot.networking.networkmessage.NetworkRequest
import net.dankrushen.tdhbot.networking.networkmessage.NetworkResponse
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import java.net.Socket
import java.nio.file.Paths

fun main() {
    BotUtils.tokenFile.createNewFile()

    val tdhBot = TDHBot(BotUtils.tokenFile.readText())
    tdhBot.execute()
}

class TDHBot : IClientConnectListener, IClientControllerListener {
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

        builder.addCommand(ConnectAccount(this))
        builder.addCommand(DisconnectAccount(this))

        builder.addCommand(ManualConnectAccount(this))
        builder.addCommand(ManualFinishConnection(this))
        builder.addCommand(ManualDisconnectAccount(this))

        builder.addCommand(PrintArgs(this))
        builder.addCommand(ListAccounts(this))

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClientRequest(controller: ClientController, request: NetworkRequest): NetworkResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClientResponse(controller: ClientController, response: NetworkResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}