package me.ihdeveloper.react.gui

import org.bukkit.plugin.java.JavaPlugin

/**
 * The main class for the plugin
 */
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
