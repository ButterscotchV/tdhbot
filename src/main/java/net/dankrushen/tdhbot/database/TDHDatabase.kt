package net.dankrushen.tdhbot.database

import jetbrains.exodus.database.TransientEntityStore
import kotlinx.dnq.XdModel
import kotlinx.dnq.query.*
import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import kotlinx.dnq.store.container.createTransientEntityStore
import kotlinx.dnq.util.initMetaData
import net.dankrushen.tdhbot.database.user.XdUser
import java.nio.file.Path

class TDHDatabase {

    val xodusStore: TransientEntityStore

    constructor(database: Path, environmentName: String = "db") {
        // Register persistent classes
        XdModel.registerNodes(XdUser)

        // Initialize Xodus persistent storage
        xodusStore = createTransientEntityStore(
                dbFolder = database.toFile(),
                environmentName = environmentName
        )

        // Initialize Xodus-DNQ metadata
        initMetaData(XdModel.hierarchy, xodusStore)
    }

    fun getUserDiscordOrSteam(discordId: String? = null, steamId: String? = null): XdUser? {
        return ThreadLocalStoreContainer.withStore(xodusStore) { XdUser.query((XdUser::discordId eq discordId) or (XdUser::steamId eq steamId)).firstOrNull() }
    }

    fun containsUserDiscordOrSteam(discordId: String? = null, steamId: String? = null): Boolean {
        return ThreadLocalStoreContainer.withStore(xodusStore) { XdUser.query((XdUser::discordId eq discordId) or (XdUser::steamId eq steamId)).any() }
    }

    fun makeUser(discordId: String, steamId: String): XdUser {
        return ThreadLocalStoreContainer.withStore(xodusStore) {
            if (containsUserDiscordOrSteam(discordId, steamId))
                throw IllegalArgumentException("Duplicate user, Discord ID or Steam ID already exist within the database")

            XdUser.new {
                this.discordId = discordId
                this.steamId = steamId
            }
        }
    }

    fun makeUserOrIfExistsReturnNull(discordId: String, steamId: String): XdUser? {
        return ThreadLocalStoreContainer.withStore(xodusStore) {
            var user: XdUser? = null

            if (!containsUserDiscordOrSteam(discordId, steamId)) {
                user = XdUser.new {
                    this.discordId = discordId
                    this.steamId = steamId
                }
            }

            user
        }
    }
}