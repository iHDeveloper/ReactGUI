package me.ihdeveloper.react.gui

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

/**
 * Represents a state holder that can be triggered when the data changed
 */
abstract class GUIComponent {
    open var eventHandler: GUIEventListener? = null
        protected set

    internal var isUpdated: Boolean = true

    protected fun update() { isUpdated = true }

    /**
     * Returns the rendered component as [ItemStack]
     */
    abstract fun render(): ItemStack
}

/**
 * Holds the components as state and keeps track of them
 */
open class GUIContainer(
        private val columns: Int = 6
) {
    protected val components = mutableMapOf<Int, GUIComponent>()

    /**
     * Sets the component in certain position of the container
     */
    open fun setComponent(x: Int, y: Int, component: GUIComponent?) {
        val finalX = min(max(x, 1), 9) - 1
        val finalY = min(max(y, 1), columns) - 1

        setComponent((9 * finalY) + finalX, component)
    }

    /**
     * Sets the component in certain index of the container
     */
    open fun setComponent(index: Int, component: GUIComponent?) {
        if (component == null) {
            components.remove(index)
            return
        }

        components[index] = component
        component.isUpdated = false
    }

    /**
     * Returns a component by certain index
     */
    fun getComponent(index: Int): GUIComponent? = components[index]
}

/**
 * Represents a container that's being shown as inventory to the player
 */
open class GUIScreen(
        protected val columns: Int,
        title: String,

        /** One player can use this screen */
        private val oneUseOnly: Boolean = true
) : GUIContainer(columns) {
    var eventHandler: GUIScreenListener? = null
        protected set

    internal var closedByAPI = false

    private val inventory = Bukkit.createInventory(null, columns * 9, title)
    private var alreadyUsed = false
    private var hasBeenRendered = false

    override fun setComponent(index: Int, component: GUIComponent?) {
        if (component == null) {
            inventory.setItem(index, null)
            super.setComponent(index, component)
            return
        }

        super.setComponent(index, component)

        if (hasBeenRendered) {
            inventory.setItem(index, component.render())
        }
    }

    internal fun open(player: Player) {
        if (oneUseOnly && alreadyUsed) {
            throw IllegalStateException("This screen has already been used by a player!")
        }
        alreadyUsed = true

        if (!hasBeenRendered) {
            components.forEach { (index, component) ->
                inventory.setItem(index, component.render())
            }

            hasBeenRendered = true
        }

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
                    (eventHandler as GUIClickListener).run {
                        if (action === InventoryAction.PICKUP_ALL)
                            onLeftClick(player)
                        else
                            onRightClick(player)
                    }

                    /** Triggers the whole screen to be re-rendered */
                    screen.reRender()
                }
            }
        }
    }

    @EventHandler
    @Suppress("UNUSED")
    fun onInventoryClose(event: InventoryCloseEvent) {
        event.run {
            // TODO handle errors coming from here
            close(player as Player)
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

        if (currentScreen.closedByAPI)
            return

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

/**
 * Represents a GUI listener that listens to screen events
 */
interface GUIScreenListener : GUIEventListener {

    /**
     * Triggered when a player opens the screen
     */
    fun onOpen(player: Player)

    /**
     * Triggered when a player closes the screen
     */
    fun onClose(player: Player, forced: Boolean)
}

/** Used to listen to click events */
interface GUIClickListener : GUIEventListener {

    /**
     * Triggered when a player lefts click a clickable component
     */
    fun onLeftClick(player: Player) {}

    /**
     * Triggered when a player rights click a clickable component
     */
    fun onRightClick(player: Player) {}
}
