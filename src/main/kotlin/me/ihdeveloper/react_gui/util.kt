package me.ihdeveloper.react_gui

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

internal fun itemStack(
        material: Material,
        amount: Int = 1,
        type: Short = 0,
        block: ItemStack.() -> Unit
): ItemStack {
    val itemStack = ItemStack(material, amount, type)
    block(itemStack)
    return itemStack
}

internal fun ItemStack.meta(block: ItemMeta.() -> Unit) {
    itemMeta = itemMeta.apply {
        block(this)
    }
}
