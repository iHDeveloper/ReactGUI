package me.ihdeveloper.react_gui.std

import me.ihdeveloper.react_gui.GUIComponent
import me.ihdeveloper.react_gui.itemStack
import me.ihdeveloper.react_gui.meta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class GUIImage(
        private val title: String,
        private val description: Array<String> = arrayOf(),
        private val material: Material,
        private val amount: Int = 1,
        private val data: Short = 0
) : GUIComponent() {
    override fun render(): ItemStack {
        return itemStack(material, amount, data) {
            meta {
                displayName = title

                lore = description.toMutableList()
            }
        }
    }
}
