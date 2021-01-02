package me.ihdeveloper.react_gui.std

import me.ihdeveloper.react_gui.GUIComponent
import me.ihdeveloper.react_gui.itemStack
import me.ihdeveloper.react_gui.meta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.math.max

class GUIProgressBarPart : GUIComponent() {
    enum class State {
        EMPTY,
        INCOMPLETE,
        FULL;
    }

    var state: State = State.EMPTY
        set(value) {
            update()
            field = value
        }

    override fun render(): ItemStack {
        return itemStack(Material.STAINED_GLASS_PANE, 1, when(state) {
            State.EMPTY -> 14
            State.INCOMPLETE -> 4
            State.FULL -> 5
        }) {
            meta {
                displayName = "§8"
            }
        }
    }
}

class GUIProgressGroup(
        private val max: Int,
        current: Int = 0,
) {
    var current: Int = current
        set(value) {
            field = value
            update()
        }

    private val parts = arrayListOf<GUIProgressBarPart>()

    fun add(part: GUIProgressBarPart) {
        parts.add(part)

        update()
    }

    private fun update() {
        val percentagePerPart = 100 / parts.size
        val currentPercentage = (current * 100) / max

        for (index in parts.indices) {
            val part = parts[index]
            val previousPartPercentage = percentagePerPart * index
            val nextPartPercentage = percentagePerPart * (index + 1)

            part.state = when {
                currentPercentage == 0 -> GUIProgressBarPart.State.EMPTY
                currentPercentage in previousPartPercentage until nextPartPercentage -> GUIProgressBarPart.State.INCOMPLETE
                currentPercentage < previousPartPercentage -> GUIProgressBarPart.State.EMPTY
                currentPercentage >= nextPartPercentage -> GUIProgressBarPart.State.FULL
                else -> GUIProgressBarPart.State.EMPTY
            }
        }
    }
}
