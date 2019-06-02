package net.dankrushen.tdhbot.networking.networkmessages

enum class NetworkMessageType(val id: Int) {
    MESSAGE(0), REQUEST(1), RESPONSE(2);

    companion object {
        fun fromId(id: Int): NetworkMessageType {
            for (value in values()) {
                if (value.id == id)
                    return value
            }

            throw IllegalArgumentException("NetworkMessageType with provided id was not found")
        }
    }

    override fun toString(): String {
        return id.toString()
    }
}