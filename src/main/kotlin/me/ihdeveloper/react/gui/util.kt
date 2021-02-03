@file:JvmName("GUIUtils")

package me.ihdeveloper.react.gui

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun itemStack(
        material: Material,
        amount: Int = 1,
        type: Short = 0,
        block: ItemStack.() -> Unit
): ItemStack {
    val itemStack = ItemStack(material, amount, type)
    block(itemStack)
    return itemStack
}

fun ItemStack.meta(block: ItemMeta.() -> Unit) {
    val newItemMeta = itemMeta
    block(newItemMeta)
    itemMeta = newItemMeta
}
