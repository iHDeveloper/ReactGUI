@file:JvmName("GUIUtils")

package me.ihdeveloper.react.gui

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

inline fun itemStack(
        material: Material,
        amount: Int = 1,
        type: Short = 0,
        block: ItemStack.() -> Unit
): ItemStack {
    val itemStack = ItemStack(material, amount, type)
    block(itemStack)
    return itemStack
}

inline fun ItemStack.meta(clearLore: Boolean = false, block: ItemMeta.() -> Unit) {
    val newItemMeta = itemMeta

    newItemMeta.run {
        if (lore == null) {
            lore = mutableListOf()
        }

        if (clearLore) {
            lore.clear()
        }
    }

    block(newItemMeta)
    itemMeta = newItemMeta
}
