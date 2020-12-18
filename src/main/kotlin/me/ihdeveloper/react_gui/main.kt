package me.ihdeveloper.react_gui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UNUSED")
class Main : JavaPlugin() {
    companion object {
        internal lateinit var instance: Main
    }

    override fun onEnable() {
        instance = this

        server.consoleSender.sendMessage("§7[§eReact GUI§7] §eStarting the screen manager...")
        GUIScreenManager.start()

        server.consoleSender.sendMessage("§eReact GUI §ais enabled! §ePlugin By §3@iHDeveloper")
    }

    override fun onDisable() {

        server.consoleSender.sendMessage("§7[§eReact GUI§7] §eShutting down screen manager safely...")
        GUIScreenManager.stop()

        server.consoleSender.sendMessage("§eReact GUI §cis disabled! §ePlugin By §3@iHDeveloper")
    }

}

/** A bridge between Bukkit Events and Screens */
internal object GUIScreenManager : Runnable {
    private val players = mutableMapOf<Player, GUIScreen>()
    private var taskId: Int = -1

    /** Used to re-render screens */
    override fun run() {
        val updated = mutableSetOf<GUIScreen>()

        players.values.forEach {
            if (!updated.contains(it)) {
                updated.add(it)

                // TODO handle errors from here as well
                it.reRender()
            }
        }
    }

    /** Called by the GUIScreen. Stores info about the player's screen and close the current safely */
    internal fun open(screen: GUIScreen, player: Player) {
        val currentScreen = players.remove(player)
        if (currentScreen != null) {
            // TODO fire close event
        }

        players[player] = screen
    }

    /** Called by GUIScreen. Removes info about the player's screen */
    internal fun close(player: Player) {
        val currentScreen = players.remove(player) ?: return
        currentScreen.close(player)
    }

    /** Called by Main. Used to setup the task for re-rendering process */
    internal fun start() {
        taskId = Bukkit.getScheduler().runTaskTimer(Main.instance, this, 0L, 1L).taskId
    }

    /** Called by Main. Used to stop the task for re-rendering process */
    internal fun stop() {
        players.forEach { (player, screen) ->
            // TODO handle error coming from here
            screen.close(player, true)
        }

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId)
        }
    }
}
