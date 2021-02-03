package me.ihdeveloper.react.gui.std

import me.ihdeveloper.react.gui.GUIClickListener
import me.ihdeveloper.react.gui.GUIComponent
import me.ihdeveloper.react.gui.GUIEventListener
import me.ihdeveloper.react.gui.itemStack
import me.ihdeveloper.react.gui.meta
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

    var stateHandler: ((player: Player, oldValue: Boolean, newValue: Boolean) -> Unit)? = null

    init {
        super.eventHandler = this
    }

    override fun onLeftClick(player: Player) {
        isChecked = !isChecked
        stateHandler?.invoke(player, !isChecked,  isChecked)
    }

    override fun render(): ItemStack {
        return itemStack(Material.INK_SACK, 1, if (isChecked) CHECKBOX_ON else CHECKBOX_OFF) {
            meta(true) {
                displayName = "§r$name"

                lore.apply {
                    add("§7Click to§${if (isChecked) "c disable" else "a enable"}")
                    add("§7")
                    description.forEach { add(it) }
                }
            }
        }
    }
}

class GUIRadioGroup(

        /** True if one option is required to be enabled */
        private val required: Boolean = false
) {
    private val checkboxes = mutableListOf<GUICheckbox>()

    fun add(checkbox: GUICheckbox) {
        checkbox.stateHandler = { _: Player, old: Boolean, new: Boolean ->
            if (required && old) {
                checkbox.isChecked = true
            } else {
                if (old != new) {
                    checkboxes.forEach {
                        if (it !== checkbox) {
                            it.isChecked = false
                        }
                    }
                }
            }
        }

        checkboxes.add(checkbox)
    }

    fun clear() {
        checkboxes.forEach {
            it.stateHandler = null
        }

        checkboxes.clear()
    }
}
