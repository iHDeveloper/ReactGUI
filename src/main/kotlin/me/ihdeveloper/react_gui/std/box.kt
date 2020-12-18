package me.ihdeveloper.react_gui.std

import me.ihdeveloper.react_gui.GUIComponent
import me.ihdeveloper.react_gui.itemStack
import me.ihdeveloper.react_gui.meta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

private const val CHECKBOX_ON: Short = 10
private const val CHECKBOX_OFF: Short = 8

class GUICheckbox(
        checked: Boolean = false,
        private val name: String,
        private val description: Array<String>,
) : GUIComponent() {
    var checked: Boolean = checked
        set(value) {
            update()
            field = value
        }

    override fun render(): ItemStack {
        return itemStack(Material.INK_SACK, 1, if (checked) CHECKBOX_ON else CHECKBOX_OFF) {
            meta {
                displayName = "§f$name"

                lore = arrayListOf<String>().apply {
                    add("§7Click to§${if (checked) "c disable" else "a enable"}")
                    add("§7")
                    addAll(description)
                }
            }
        }
    }
}
