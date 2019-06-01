package net.dankrushen.tdhbot.connector

import net.dankrushen.tdhbot.database.user.XdUser

class ConnectRequest(var discordId: String? = null, var steamId: String? = null, val requestKey: Int) {

    var returnedUser: XdUser? = null

    fun isFilled(): Boolean {
        return !(discordId.isNullOrBlank() || steamId.isNullOrBlank())
    }
}