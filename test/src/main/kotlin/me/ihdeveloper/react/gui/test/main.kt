package me.ihdeveloper.react.gui.test

import me.ihdeveloper.react.gui.GUIScreen
import me.ihdeveloper.react.gui.closeScreen
import me.ihdeveloper.react.gui.openScreen
import me.ihdeveloper.react.gui.std.GUIButton
import me.ihdeveloper.react.gui.std.GUICheckbox
import me.ihdeveloper.react.gui.std.GUIImage
import me.ihdeveloper.react.gui.test.gui.KotlinGUIScreen
import me.ihdeveloper.react.gui.test.screen.DynamicInteractiveScreen
import me.ihdeveloper.react.gui.test.screen.FixedInteractiveScreen
import me.ihdeveloper.react.gui.test.screen.JavaGUIScreen
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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

        if (type == "native") {
            player.sendMessage("§eRunning native inventory...")

            val startTimestamp = System.nanoTime()
            val nativeInventory = Bukkit.createInventory(null, 3 * 9, "Native Inventory")
            nativeInventory.setItem(11, ItemStack(Material.OBSIDIAN))
            nativeInventory.setItem(13, ItemStack(Material.IRON_SWORD))
            nativeInventory.setItem(15, ItemStack(Material.COAL_BLOCK))
            player.openInventory(nativeInventory)
            player.sendMessage("§eBuilt the native screen in §c${(System.nanoTime() - startTimestamp) / 1e6}ms")
            return true
        }

        var startTimestamp = System.nanoTime()
        val screen = buildTestScreen(type)
        player.sendMessage("§eBuilt the reactive screen in §c${(System.nanoTime() - startTimestamp) / 1e6}ms")

        if (screen == null) {
            player.sendMessage("§cThere's no test screen with that name!")
            return true
        }

        startTimestamp = System.nanoTime()
        player.openScreen(screen)
        player.sendMessage("§eOpening the screen in §c${(System.nanoTime() - startTimestamp) / 1e6}ms")
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
            setComponent(3, 2, GUIImage("Image", arrayOf("A simple image"), Material.CAKE))
            setComponent(5, 2, GUICheckbox(false, "Checkbox 1", arrayOf("§7A simple checkbox")))
            setComponent(7, 2, GUIButton("Simple Button", arrayOf("A simple button"), Material.STONE_BUTTON).apply {
                onLeftClick = { player ->
                    player.closeScreen()
                }
            })
        }
    }
    "java" -> {
        JavaGUIScreen()
    }
    "kotlin" -> {
        KotlinGUIScreen()
    }
    "fixed-interactive" -> {
        FixedInteractiveScreen()
    }
    "dynamic-interactive" -> {
        DynamicInteractiveScreen()
    }
    else -> null
}
