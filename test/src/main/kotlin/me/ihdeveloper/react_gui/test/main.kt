package me.ihdeveloper.react_gui.test

import me.ihdeveloper.react_gui.GUIScreen
import me.ihdeveloper.react_gui.openScreen
import me.ihdeveloper.react_gui.std.GUICheckbox
import me.ihdeveloper.react_gui.std.GUIImage
import org.bukkit.Material
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
        val screen = buildTestScreen(type)

        if (screen == null) {
            player.sendMessage("§cThere's no test screen with that name!")
            return true
        }

        player.openScreen(screen)
        return true
    }

}

private fun screen(columns: Int, title: String, oneUseOnly: Boolean = true, block: GUIScreen.() -> Unit): GUIScreen {
    return GUIScreen(columns, title, oneUseOnly).apply {
        block(this)
    }
}

private fun buildTestScreen(type: String): GUIScreen? = when(type) {
    "plain" -> {
        GUIScreen(
                columns = 3,
                title = "Plain Screen"
        )
    }
    "sample" -> {
        screen(3, "Sample Screen") {
            setComponent(4, 2, GUIImage("Image", arrayOf("A simple image"), Material.CAKE))
            setComponent(6, 2, GUICheckbox(false, "Checkbox 1", arrayOf("§7A simple checkbox")))
        }
    }
    "java" -> {
        JavaGUIScreen()
    }
    else -> null
}
