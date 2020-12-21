@file:JvmName("ReactGUI")

package me.ihdeveloper.react_gui

import org.bukkit.entity.Player

fun Player.openScreen(screen: GUIScreen) {
    screen.open(this)
}

fun Player.closeScreen() = closeScreen(false)
fun Player.closeScreen(forced: Boolean = false) {
    GUIScreenManager.players[this]?.let { screen ->
        screen.closedByAPI = true
        screen.close(this, forced)
    }
}
