@file:JvmName("ReactGUI")

package me.ihdeveloper.react.gui

import org.bukkit.entity.Player

/**
 * Opens the screen to the player
 */
fun Player.openScreen(screen: GUIScreen) {
    screen.open(this)
}

/**
 * Closes the current screen to the player
 */
fun Player.closeScreen() = closeScreen(false)

/**
 * Closes the current screen to the player (If true force closes the screen)
 */
fun Player.closeScreen(forced: Boolean = false) {
    GUIScreenManager.players[this]?.let { screen ->
        screen.closedByAPI = true
        screen.close(this, forced)
    }
}
