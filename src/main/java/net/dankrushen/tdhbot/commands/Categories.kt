package net.dankrushen.tdhbot.commands

import com.jagrosh.jdautilities.command.Command
import net.dankrushen.tdhbot.BotUtils
import net.dv8tion.jda.core.Permission

object Categories {
    const val noPermissionError = "${BotUtils.errorEmoji} You do not have the required permission(s) to run this command."

    val general = Command.Category("General")

    val accountManagement = Command.Category("Account Management")

    val admin = Command.Category("Administrator Commands", noPermissionError) { cmdEvent ->
        cmdEvent.member != null && cmdEvent.member.hasPermission(Permission.ADMINISTRATOR)
    }

    val dev = Command.Category("Bot Development Commands", noPermissionError) { cmdEvent ->
        cmdEvent.isOwner
    }
}