package me.ihdeveloper.react_gui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
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

class GUIScreen(
        columns: Int,
        title: String,

        /** One player can use this screen */
        private val oneUseOnly: Boolean = true
) {
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

        // TODO tell GUI manager that this player opened this screen
        GUIScreenManager.open(this, player)

        // TODO fire open event
        player.openInventory(inventory)
    }

    internal fun close(player: Player, forced: Boolean = false) {
        GUIScreenManager.close(player)
        player.closeInventory()

        // TODO fire close event
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

                if (eventHandler is GUIClickEvent &&
                        (action == InventoryAction.PICKUP_ONE
                        || action == InventoryAction.PICKUP_SOME
                        || action == InventoryAction.PICKUP_HALF
                        || action == InventoryAction.PICKUP_ALL)
                ) {
                    (eventHandler as GUIClickEvent).onClick(player)
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
        val currentScreen = players.remove(player)
        if (currentScreen != null) {
            // TODO fire close event
        }

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

/** Used to listen to click events */
interface GUIClickEvent : GUIEventListener {
    fun onClick(player: Player) {}
}
