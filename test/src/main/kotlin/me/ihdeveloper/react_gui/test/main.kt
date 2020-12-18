package me.ihdeveloper.react_gui.test

import me.ihdeveloper.react_gui.GUIScreen
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UNUSED")
class Main : JavaPlugin(), Listener {

    override fun onEnable() {
        server.consoleSender.sendMessage("§eReact GUI §bTEST §ais enabled! §ePlugin By §3@iHDeveloper")
    }

    override fun onDisable() {
        server.consoleSender.sendMessage("§eReact GUI §bTEST §cis disabled! §ePlugin By §3@iHDeveloper")
    }

    @EventHandler
    @Suppress("UNUSED")
    private fun onJoin(event: PlayerJoinEvent) {
        event.run {
            val screen = GUIScreen(
                    columns = 3,
                    title = "Plain Screen"
            )

            // TODO open screen for the player (via API call)
        }
    }

}
