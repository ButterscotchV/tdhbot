package net.dankrushen.tdhbot.database.user

import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.XdEntity
import kotlinx.dnq.XdNaturalEntityType
import kotlinx.dnq.store.container.ThreadLocalStoreContainer
import kotlinx.dnq.xdBlobStringProp
import kotlinx.dnq.xdRequiredStringProp

class XdUser(entity: Entity) : XdEntity(entity) {

    companion object : XdNaturalEntityType<XdUser>(storeContainer = ThreadLocalStoreContainer)

    var discordId by xdRequiredStringProp()
    var steamId by xdRequiredStringProp()

    var discordRoles by xdBlobStringProp<XdUser>()

    var customTag by xdBlobStringProp<XdUser>()

    fun getRoles(): Array<String> {
        return discordRoles?.split(",")?.toTypedArray() ?: emptyArray()
    }

    fun setRoles(roles: Array<String>) {
        discordRoles = roles.joinToString(",")
    }

    fun addRoles(roles: Array<String>) {
        if (roles.isEmpty())
            return

        val newRoles = mutableListOf<String>()

        newRoles.addAll(getRoles())
        newRoles.addAll(roles)

        setRoles(newRoles.toTypedArray())
    }

    fun addRolesIfMissing(roles: Array<String>) {
        if (roles.isEmpty())
            return

        val newRoles = mutableListOf(*getRoles())

        for (role in roles) {
            if (!newRoles.contains(role)) {
                newRoles.add(role)
            }
        }

        setRoles(newRoles.toTypedArray())
    }

    fun removeRoles(roles: Array<String>) {
        if (roles.isEmpty())
            return

        val newRoles = mutableListOf<String>()

        newRoles.addAll(getRoles())
        newRoles.removeAll(roles)

        setRoles(newRoles.toTypedArray())
    }
}