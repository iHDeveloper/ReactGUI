package me.ihdeveloper.react_gui

import org.bukkit.plugin.java.JavaPlugin

@Suppress("UNUSED")
class Main : JavaPlugin() {

    override fun onEnable() {
        server.consoleSender.sendMessage("§eReact GUI §ais enabled! §ePlugin By §3@iHDeveloper")
    }

    override fun onDisable() {
        server.consoleSender.sendMessage("§eReact GUI §cis disabled! §ePlugin By §3@iHDeveloper")
    }

}
