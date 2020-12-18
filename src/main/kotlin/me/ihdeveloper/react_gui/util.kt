package me.ihdeveloper.react_gui

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

internal fun ItemStack.itemMeta(block: ItemMeta.() -> Unit) {
    itemMeta = itemMeta.apply {
        block(this)
    }
}
