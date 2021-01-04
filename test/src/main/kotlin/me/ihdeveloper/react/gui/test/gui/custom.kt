package me.ihdeveloper.react.gui.test.gui

import me.ihdeveloper.react.gui.GUIClickListener
import me.ihdeveloper.react.gui.GUIComponent
import me.ihdeveloper.react.gui.GUIScreen
import me.ihdeveloper.react.gui.GUIScreenListener
import me.ihdeveloper.react.gui.itemStack
import me.ihdeveloper.react.gui.meta
import me.ihdeveloper.react.gui.std.GUIProgressBarPart
import me.ihdeveloper.react.gui.std.GUIProgressGroup
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import kotlin.math.min
import kotlin.math.max

private val gameModes = arrayOf("Survival", "Creative", "Spectator")

internal class KotlinGUIScreen : GUIScreen(4, "§0» §3[Kotlin] §8Custom Screen"), GUIScreenListener {
    private val group = ExpGroup()
    private val gameModeSwitch = GameModeComponent()

    init {
        for (i in 2..8) {
            setComponent(i, 2, GUIProgressBarPart().also { group.add(it) })
        }
        setComponent(4, 3, ExpManageComponent().also { group.button = it })

        setComponent(6, 3, gameModeSwitch)

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

    override fun onLeftClick(player: Player) {
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

internal class ExpManageComponent : GUIComponent(), GUIClickListener {
    var exp: Int = 0
        set(value) {
            update()
            field = value
        }

    var maxExp: Int = 0

    var onAdd: (() -> Unit)? = null
    var onReset: (() -> Unit)? = null

    init {
        eventHandler = this
    }

    override fun onLeftClick(player: Player) {
        onAdd?.invoke()
        with (player) { playSound(location, Sound.ORB_PICKUP, 0.5F, 2.5F) }
    }

    override fun onRightClick(player: Player) {
        onReset?.invoke()
        with (player) { playSound(location, Sound.ORB_PICKUP, 1F, 5F) }
    }

    override fun render(): ItemStack {
        return itemStack(Material.EXP_BOTTLE) {
            meta {
                displayName = "§eManage §9Experience"
                lore = arrayListOf(
                        "§7Increase the experience value",
                        "§8» §9Exp: §e$exp§7/§6$maxExp",
                        "§7",
                        "§aAdds §a+§65 §9Experience §7(Left Click)",
                        "§cReset Experience §7(Right Click)"
                )
            }
        }
    }
}

internal class ExpGroup(
	private val maxExp: Int = 200
) {
    var button: ExpManageComponent? = null
        set(value) {
            value?.onAdd = {
                exp += 5
                update()
            }
            value?.onReset = {
                exp = 0
                update()
            }

	    value?.maxExp = maxExp
            field = value
        }

    private val group = GUIProgressGroup(maxExp, 0)

    fun add(part: GUIProgressBarPart) = group.add(part)

    internal var exp: Int = 0

    private fun update() {
        button?.exp = exp
        group.current = exp
    }
}
