package me.ihdeveloper.react_gui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.min
import kotlin.math.max

abstract class GUIComponent {
    var isUpdated: Boolean = false
        private set

    protected fun update() { isUpdated = true }

    abstract fun render(): ItemStack
}

class GUIScreen(
        columns: Int,
        title: String,

        /** One player can use this screen */
        private val oneUseOnly: Boolean = true
) {
    private val components = mutableMapOf<Int, GUIComponent>()
    private val inventory = Bukkit.createInventory(null, columns * 9, title)
    private var alreadyUsed = false

    fun setItem(x: Int, y: Int, component: GUIComponent) {
        val finalX = min(max(x, 1), 9) - 1
        val finalY = min(max(y, 1), 6) - 1

        setItem((9 * finalY) + finalX, component)
    }

    fun setItem(index: Int, component: GUIComponent) {
        components[index] = component
        inventory.setItem(index, component.render())
    }

    internal fun open(player: Player) {
        if (oneUseOnly && alreadyUsed) {
            throw IllegalStateException("This screen has already been used by a player!")
        }
        alreadyUsed = true

        // TODO tell GUI manager that this player opened this screen

        // TODO fire open event
        player.openInventory(inventory)
    }

    internal fun close(player: Player) {
        // TODO fire close event

        player.closeInventory()
    }

    internal fun reRender() {
        components.forEach { (index, component) ->
            if (component.isUpdated)
                setItem(index, component)
        }
    }
}
