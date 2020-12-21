package me.ihdeveloper.react_gui.std

import me.ihdeveloper.react_gui.GUIClickListener
import me.ihdeveloper.react_gui.GUIComponent
import me.ihdeveloper.react_gui.GUIEventListener
import me.ihdeveloper.react_gui.itemStack
import me.ihdeveloper.react_gui.meta
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

private const val CHECKBOX_ON: Short = 10
private const val CHECKBOX_OFF: Short = 8

class GUICheckbox(
        checked: Boolean = false,
        private val name: String,
        private val description: Array<String>,
) : GUIComponent(), GUIEventListener, GUIClickListener {
    var isChecked: Boolean = checked
        set(value) {
            update()
            field = value
        }

    init {
        eventHandler = this
    }

    override fun onClick(player: Player) {
        isChecked = !isChecked
    }

    override fun render(): ItemStack {
        return itemStack(Material.INK_SACK, 1, if (isChecked) CHECKBOX_ON else CHECKBOX_OFF) {
            meta {
                displayName = "§f$name"

                lore = arrayListOf<String>().apply {
                    add("§7Click to§${if (isChecked) "c disable" else "a enable"}")
                    add("§7")
                    addAll(description)
                }
            }
        }
    }
}
