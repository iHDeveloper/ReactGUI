package me.ihdeveloper.react_gui.test

import me.ihdeveloper.react_gui.GUIScreen
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UNUSED")
class Main : JavaPlugin() {

    override fun onEnable() {
        getCommand("test-screen").apply {
            executor = TestScreenCommand
        }

        server.consoleSender.sendMessage("§eReact GUI §bTEST §ais enabled! §ePlugin By §3@iHDeveloper")
    }

    override fun onDisable() {
        server.consoleSender.sendMessage("§eReact GUI §bTEST §cis disabled! §ePlugin By §3@iHDeveloper")
    }

}

/** Used for testing different screen behaviours built using the library */
object TestScreenCommand : CommandExecutor {

    override fun onCommand(player: CommandSender?, cmd: Command?, label: String?, args: Array<out String>?): Boolean {
        if (player !is Player) {
            player!!.sendMessage("§cYou should be a player to execute this command!")
            return true
        }

        if (!player.isOp) {
            player.sendMessage("§cYou don't have the permission to execute this command!")
            return true
        }

        if (args!!.isEmpty())
            return false

        val type = args[0]

        when (type) {
            "plain" -> {
                val screen = GUIScreen(
                        columns = 3,
                        title = "Plain Screen"
                )

                // TODO open screen for the player (via API call)
            }
        }
        return true
    }

}
