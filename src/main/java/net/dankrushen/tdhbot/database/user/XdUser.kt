package net.dankrushen.tdhbot.database.user

import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.XdEntity
import kotlinx.dnq.XdNaturalEntityType
import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import kotlinx.dnq.xdMutableSetProp
import kotlinx.dnq.xdRequiredStringProp

class XdUser(entity: Entity) : XdEntity(entity) {

    companion object : XdNaturalEntityType<XdUser>(storeContainer = ThreadLocalStoreContainer)

    var discordId by xdRequiredStringProp()
    var steamId by xdRequiredStringProp()

    val discordRoles by xdMutableSetProp<XdUser, String>()

    fun setRoles(roles: Array<String>) {
        discordRoles.clear()
        discordRoles.addAll(roles)
    }

    fun addRoles(roles: Array<String>) {
        discordRoles.addAll(roles)
    }

    fun addRolesIfMissing(roles: Array<String>) {
        for (role in roles) {
            if (!discordRoles.contains(role)) {
                discordRoles.add(role)
            }
        }
    }

    fun removeRoles(roles: Array<String>) {
        discordRoles.removeAll(roles)
    }
}