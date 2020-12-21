package me.ihdeveloper.react_gui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min

abstract class GUIComponent {
    var eventHandler: GUIEventListener? = null
        protected set

    internal var isUpdated: Boolean = true

    protected fun update() { isUpdated = true }

    abstract fun render(): ItemStack
}

open class GUIScreen(
        columns: Int,
        title: String,

        /** One player can use this screen */
        private val oneUseOnly: Boolean = true
) {
    var eventHandler: GUIScreenListener? = null
        protected set

    private val components = mutableMapOf<Int, GUIComponent>()
    private val inventory = Bukkit.createInventory(null, columns * 9, title)
    private var alreadyUsed = false

    fun setComponent(x: Int, y: Int, component: GUIComponent?) {
        val finalX = min(max(x, 1), 9) - 1
        val finalY = min(max(y, 1), 6) - 1

        setComponent((9 * finalY) + finalX, component)
    }

    fun setComponent(index: Int, component: GUIComponent?) {
        if (component == null) {
            components.remove(index)
            inventory.setItem(index, null)
            return
        }

        components[index] = component

        component.isUpdated = false
        inventory.setItem(index, component.render())
    }

    fun getComponent(index: Int): GUIComponent? = components[index]

    internal fun open(player: Player) {
        if (oneUseOnly && alreadyUsed) {
            throw IllegalStateException("This screen has already been used by a player!")
        }
        alreadyUsed = true

        GUIScreenManager.open(this, player)

        eventHandler?.onOpen(player)
        player.openInventory(inventory)
    }

    internal fun close(player: Player, forced: Boolean = false) {
        GUIScreenManager.close(player)
        player.closeInventory()

        eventHandler?.onClose(player, forced)
    }

    internal fun reRender() {
        components.forEach { (index, component) ->
            if (component.isUpdated)
                setComponent(index, component)
        }
    }
}

/** A bridge between Bukkit Events and Screens */
internal object GUIScreenManager : Runnable, Listener {
    internal val players = mutableMapOf<Player, GUIScreen>()

    private var taskId: Int = -1

    @EventHandler
    @Suppress("UNUSED")
    fun onInventoryAction(event: InventoryClickEvent) {
        event.run {
            val player = whoClicked as Player
            val screen = players[player] ?: return

            isCancelled = true

            val component = screen.getComponent(slot) ?: return

            component.run {
                if (eventHandler == null)
                    return

                if (eventHandler is GUIClickListener &&
                        (action == InventoryAction.PICKUP_ONE
                        || action == InventoryAction.PICKUP_SOME
                        || action == InventoryAction.PICKUP_HALF
                        || action == InventoryAction.PICKUP_ALL)
                ) {
                    (eventHandler as GUIClickListener).onClick(player)
                }
            }
        }
    }

    @EventHandler
    @Suppress("UNUSED")
    fun onInventoryClose(event: InventoryCloseEvent) {
        event.run {
            val screen = players[player]

            // TODO handle errors coming from here
            screen?.close(player as Player, false)
        }
    }

    @EventHandler
    @Suppress("UNUSED")
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.run {

            /** The player's connection got closed and the screen has to be closed forcibly and not safely */
            players[player]?.close(player, true)
        }
    }

    /** Used to re-render screens */
    override fun run() {
        val updated = mutableSetOf<GUIScreen>()

        players.values.forEach {
            if (!updated.contains(it)) {
                updated.add(it)

                // TODO handle errors from here as well
                it.reRender()
            }
        }
    }

    /** Called by the GUIScreen. Stores info about the player's screen and close the current safely */
    internal fun open(screen: GUIScreen, player: Player) {
        players[player] = screen
    }

    /** Called by GUIScreen. Removes info about the player's screen */
    internal fun close(player: Player) {
        val currentScreen = players.remove(player) ?: return
        currentScreen.close(player)
    }

    /** Called by Main. Used to setup the task for re-rendering process */
    internal fun start() {
        taskId = Bukkit.getScheduler().runTaskTimer(Main.instance, this, 0L, 1L).taskId
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    /** Called by Main. Used to stop the task for re-rendering process */
    internal fun stop() {
        players.forEach { (player, screen) ->
            // TODO handle error coming from here
            screen.close(player, true)
        }

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId)
        }
    }
}

/** Used to implement event listener */
interface GUIEventListener

interface GUIScreenListener : GUIEventListener {
    fun onOpen(player: Player)
    fun onClose(player: Player, forced: Boolean)
}

/** Used to listen to click events */
interface GUIClickListener : GUIEventListener {
    fun onClick(player: Player) {}
}
