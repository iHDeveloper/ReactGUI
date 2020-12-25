package me.ihdeveloper.react_gui.test.gui

import me.ihdeveloper.react_gui.GUIClickListener
import me.ihdeveloper.react_gui.GUIComponent
import me.ihdeveloper.react_gui.GUIScreen
import me.ihdeveloper.react_gui.GUIScreenListener
import me.ihdeveloper.react_gui.itemStack
import me.ihdeveloper.react_gui.meta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import kotlin.math.min
import kotlin.math.max

private val gameModes = arrayOf("Survival", "Creative", "Spectator")

internal enum class ExpStatus(
        internal val displayName: String,
        internal val color: Short
) {
    EMPTY("§4Empty", 14),
    INCOMPLETE("§eIncomplete", 4),
    FULL("§aFull!", 5)
    ;
}

internal class KotlinGUIScreen : GUIScreen(3, "§0» §3[Kotlin] §8Custom Screen"), GUIScreenListener {
    private val group = ExpGroup()
    private val gameModeSwitch = GameModeComponent()

    init {
        for (i in 2..4) {
            setComponent(i, 2, ExpProgressComponent().also { group.components.add(it) })
        }
        setComponent(5, 2, ExpAddComponent().also { group.addButton = it })
        setComponent(6, 2, ExpResetComponent().also { group.resetButton = it })

        setComponent(8, 2, gameModeSwitch)

        eventHandler = this
    }

    override fun onOpen(player: Player) {}

    override fun onClose(player: Player, forced: Boolean) {
        if (forced) {
            Bukkit.getServer().consoleSender.sendMessage("§eCouldn't update the data for player §9${player.name} §7(FORCED_CLOSE)")
            return
        }

        player.sendMessage("§eGame Mode: §f${gameModes[gameModeSwitch.mode]}")
        player.sendMessage("§eExp: §f${group.exp}")
    }
}

internal class GameModeComponent : GUIComponent(), GUIClickListener {
    var mode: Int = 0
        set(value) {
            update()
            field = min(max(value, 0), 2)
        }

    init {
        eventHandler = this
    }

    override fun onClick(player: Player) {
        mode = (mode + 1) % 3
        with (player) { playSound(location, Sound.ORB_PICKUP, 10F, 5F) }
    }

    override fun render(): ItemStack {
        val material = when (mode) {
            0 -> Material.IRON_SWORD
            1 -> Material.GRASS
            2 -> Material.COMPASS
            else -> Material.STONE
        }

        return itemStack(material, 1) {
            meta {
                displayName = "§eToggle Game Mode"
                addItemFlags(ItemFlag.HIDE_ATTRIBUTES)

                lore = arrayListOf(
                        "§7Click to toggle your game mode.",
                        "§7",
                ).apply {
                    for (index in gameModes.indices) {
                        if (mode == index)
                            add("§8§l» §e§l${gameModes[index]}")
                        else
                            add("§8» §f${gameModes[index]}")
                    }
                }
            }
        }
    }
}

internal class ExpProgressComponent : GUIComponent() {
    var status: ExpStatus = ExpStatus.EMPTY
        set(value) {
            update()
            field = value
        }

    override fun render(): ItemStack {
        return itemStack(Material.STAINED_GLASS_PANE, 1, status.color) {
            meta {
                displayName = "§eYour Progress"

                lore = arrayListOf(
                        "§7",
                        "§7Status: ${status.displayName}"
                )
            }
        }
    }
}

internal class ExpAddComponent : GUIComponent(), GUIClickListener {
    var exp: Int = 0
        set(value) {
            update()
            field = value
        }

    var handler: ((player: Player) -> Unit)? = null

    init {
        eventHandler = this
    }

    override fun onClick(player: Player) {
        handler?.invoke(player)
    }

    override fun render(): ItemStack {
        return itemStack(Material.POTION) {
            meta {
                displayName = "§fAdd experience"
                lore = arrayListOf("§7Increase the experience value","§7","§8» §eExp: §f$exp")
            }
        }
    }
}

internal class ExpResetComponent : GUIComponent(), GUIClickListener {
    var handler: ((player: Player) -> Unit)? = null

    init {
        eventHandler = this
    }

    override fun onClick(player: Player) {
        handler?.invoke(player)
    }

    override fun render(): ItemStack {
        return itemStack(Material.POTION, 1, 8261) {
            meta {
                addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
                addItemFlags(ItemFlag.HIDE_ATTRIBUTES)

                displayName = "§fReset experience"
                lore = arrayListOf("§7Resets the experience")
            }
        }
    }
}

internal class ExpGroup {
    var addButton: ExpAddComponent? = null
        set(value) {
            value?.handler = {
                exp += 5
                update()
            }

            field = value
        }

    var resetButton: ExpResetComponent? = null
        set(value) {
            value?.handler = {
                exp = 0
                update()
            }

            field = value
        }

    val components = mutableListOf<ExpProgressComponent>()

    internal var exp: Int = 0

    private fun update() {
        addButton?.exp = exp

        val currentPercent = (exp * 100) / 50
        val percentPerStage = 100 / components.size

        for (index in 1..components.size) {
            components[index - 1].status = when {
                currentPercent in (percentPerStage * max(index - 1, 0)) + 1 until (percentPerStage * index) -> ExpStatus.INCOMPLETE
                currentPercent < (percentPerStage * index) -> ExpStatus.EMPTY
                currentPercent >= (percentPerStage * index) -> ExpStatus.FULL
                else -> ExpStatus.EMPTY
            }
        }
    }
}
