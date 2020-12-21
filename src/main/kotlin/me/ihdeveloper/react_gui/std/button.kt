package me.ihdeveloper.react_gui.std

import me.ihdeveloper.react_gui.GUIClickListener
import me.ihdeveloper.react_gui.GUIComponent
import me.ihdeveloper.react_gui.itemStack
import me.ihdeveloper.react_gui.meta
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
    var handler: ((player: Player) -> Unit)? = null

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

    override fun onClick(player: Player) {
        handler?.invoke(player)
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