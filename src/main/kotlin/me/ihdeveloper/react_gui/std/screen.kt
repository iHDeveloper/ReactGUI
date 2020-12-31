package me.ihdeveloper.react_gui.std

import me.ihdeveloper.react_gui.GUIContainer
import me.ihdeveloper.react_gui.GUIScreen
import org.bukkit.Material

private val WALL_IMAGE = GUIImage("§8", arrayOf(), Material.STAINED_GLASS_PANE, 1, 15)

abstract class GUIPaginator(
        pages: Int,
        columns: Int,
        title: String,
        oneUseOnly: Boolean = true
) : GUIScreen(columns, title, oneUseOnly) {
    init {
        if (pages < 2 || pages > 9)
            error("The number of pages in paginator should be between 2 and 9")
    }

    protected val containers = arrayListOf<GUIContainer?>()
    protected var currentPage = 1
        set(value) {
            field = value
            update()
        }

    fun addPage(container: GUIContainer) = containers.add(container)

    fun removePage(index: Int) = containers.removeAt(index - 1)

    fun getPage(index: Int): GUIContainer? = containers[index - 1]

    protected fun update() {
        updateContainer(getPage(currentPage))
        updateBar()
    }

    protected fun updateContainer(container: GUIContainer?) {
        for (index in 1..((columns - 1) * 9)) {
            setComponent(index, container?.getComponent(index))
        }
    }

    protected abstract fun updateBar()
}

open class GUIFixedPaginator(
        pages: Int,
        columns: Int,
        title: String,
        oneUseOnly: Boolean = true
) : GUIPaginator(pages, columns, title, oneUseOnly) {

    override fun updateBar() {
        for (x in 1..9) {
            if (x > containers.size) {
                setComponent(x, columns, WALL_IMAGE)
                continue
            }

            setComponent(x, columns, GUIButton(
                    "§8» §ePage §6$x",
                    arrayOf(when {
                        x == currentPage -> "§cYou already have this opened"
                        x <= containers.size -> "§aClick to open"
                        else -> "§7Nothing to open here..."
                    }),
                    Material.STAINED_GLASS_PANE,
                    1,
                    when {
                        x == currentPage -> 5
                        x <= containers.size -> 14
                        else -> 15
                    }
            ).apply {
                onLeftClick = {
                    currentPage = x
                }
            })
        }
    }

}

open class GUIDynamicPaginator(
        pages: Int,
        columns: Int,
        title: String,
        oneUseOnly: Boolean = true
) : GUIPaginator(pages, columns, title, oneUseOnly) {

    override fun updateBar() {
        setComponent(1, columns,
                if (currentPage > 1)
                    GUIButton("§8» §eFirst Page", arrayOf("§7Go to page §e1"), Material.BLAZE_ROD).apply {
                        onLeftClick = {
                            currentPage = 1
                        }
                    }
                else WALL_IMAGE)

        setComponent(2, columns,
                if (currentPage > 1)
                    GUIButton("§8» §ePrevious Page", arrayOf("§7Go to page §e${currentPage - 1}"), Material.STICK).apply {
                        onLeftClick = {
                            currentPage--
                        }
                    }
                else WALL_IMAGE)

        for (x in 3..7) {
            setComponent(x, columns, WALL_IMAGE)
        }

        setComponent(8, columns,
                if (currentPage < containers.size)
                    GUIButton("§8» §eNext Page", arrayOf("§7GO to page §e${currentPage + 1}"), Material.STICK).apply {
                        onLeftClick = {
                            currentPage++
                        }
                    }
                else WALL_IMAGE)

        setComponent(9, columns,
                if (currentPage < containers.size)
                    GUIButton("§8» §eLast Page", arrayOf("§7Go to page §e${containers.size}"), Material.BLAZE_ROD).apply {
                        onLeftClick = {
                            currentPage = containers.size
                        }
                    }
                else WALL_IMAGE)
    }

}
