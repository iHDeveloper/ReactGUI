package me.ihdeveloper.react.gui.std

import me.ihdeveloper.react.gui.GUIComponent
import me.ihdeveloper.react.gui.itemStack
import me.ihdeveloper.react.gui.meta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class GUIImage(
        private val title: String,
        private val description: Array<String> = arrayOf(),
        private val material: Material,
        private val amount: Int,
        private val data: Short
) : GUIComponent() {
    constructor(
            title: String,
            description: Array<String>,
            material: Material,
            amount: Int
    ) : this(title, description, material, amount, 0)

    constructor(
            title: String,
            description: Array<String>,
            material: Material
    ) : this(title, description, material, 1, 0)

    override fun render(): ItemStack {
        return itemStack(material, amount, data) {
            meta {
                displayName = "§r$title"

                if (lore == null) {
                    lore = mutableListOf()
                }

                lore.clear()
                for (line in description) {
                    lore.add(line)
                }
            }
        }
    }
}
