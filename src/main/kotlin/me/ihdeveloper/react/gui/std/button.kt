package me.ihdeveloper.react.gui.std

import me.ihdeveloper.react.gui.GUIClickListener
import me.ihdeveloper.react.gui.GUIComponent
import me.ihdeveloper.react.gui.itemStack
import me.ihdeveloper.react.gui.meta
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GUIButton(
        private val name: String,
        private val description: Array<String> = arrayOf(),
        private val material: Material,
        private val amount: Int,
        private val data: Short,
) : GUIComponent(), GUIClickListener {
    var onLeftClick: ((player: Player) -> Unit)? = null
    var onRightClick: ((player: Player) -> Unit)? = null

    constructor(
            name: String,
            description: Array<String> = arrayOf(),
            material: Material,
    ) : this(name, description, material, 1, 0)

    constructor(
            name: String,
            description: Array<String> = arrayOf(),
            material: Material,
            amount: Int,
    ) : this(name, description, material, amount, 0)

    init {
        eventHandler = this
    }

    override fun onLeftClick(player: Player) {
        onLeftClick?.invoke(player)
    }

    override fun onRightClick(player: Player) {
        onRightClick?.invoke(player)
    }

    override fun render(): ItemStack {
        return itemStack(material, amount, data) {
            meta {
                displayName = "§r$name"

                lore = description.toMutableList()
            }
        }
    }
}