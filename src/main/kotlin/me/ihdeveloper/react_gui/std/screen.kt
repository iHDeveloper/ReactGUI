package me.ihdeveloper.react_gui.std

import me.ihdeveloper.react_gui.GUIContainer
import me.ihdeveloper.react_gui.GUIScreen
import org.bukkit.Material

private val WALL_IMAGE = GUIImage("§8", arrayOf(), Material.STAINED_GLASS_PANE, 1, 15)
private val PAGE_NOT_FOUND_IMAGE = GUIImage("§8» §cPage Not Found", arrayOf(), Material.STAINED_GLASS_PANE, 1, 14)

open class GUIPaginator(
        pages: Int,
        columns: Int,
        title: String,
        oneUseOnly: Boolean = true
) : GUIScreen(columns, title, oneUseOnly) {
    init {
        if (pages < 2 || pages > 9)
            error("The number of pages in paginator should be between 2 and 9")
    }

    private val containers = arrayListOf<GUIContainer?>()
    private var currentPage = 1
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

    private fun updateContainer(container: GUIContainer?) {
        for (index in 1..((columns - 1) * 9)) {
            setComponent(index, container?.getComponent(index))
        }
    }

    private fun updateBar() {
        for (x in 1..9) {
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
