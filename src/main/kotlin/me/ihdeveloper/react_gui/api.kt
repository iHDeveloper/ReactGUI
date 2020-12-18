@file:JvmName("ReactGUI")

package me.ihdeveloper.react_gui

import org.bukkit.entity.Player

fun Player.openScreen(screen: GUIScreen) {
    screen.open(this)
}

fun Player.closeScreen() {
    GUIScreenManager.players[this]?.close(this, true)
}
