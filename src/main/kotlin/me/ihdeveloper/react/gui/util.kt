@file:JvmName("GUIUtils")

package me.ihdeveloper.react.gui

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * Constructs an [ItemStack]
 */
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

/**
 * Constructs an [ItemMeta]
 */
inline fun ItemStack.meta(block: ItemMeta.() -> Unit) {
    itemMeta = itemMeta.apply {
        block(this)
    }
}

/**
 * Changes the lore in the [ItemMeta] in a dynamic way
 */
inline fun ItemMeta.dynamicLore(clear: Boolean = false, block: MutableList<String>.() -> Unit) {
    var newLore: MutableList<String>? = lore

    if (newLore != null && clear) {
        newLore.clear()
    }

    if (newLore == null) {
        newLore = mutableListOf()
    }

    lore = newLore.apply {
        block(this)
    }
}
