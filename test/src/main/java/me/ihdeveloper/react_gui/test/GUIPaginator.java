package me.ihdeveloper.react_gui.test;

import kotlin.Unit;
import me.ihdeveloper.react_gui.GUIComponent;
import me.ihdeveloper.react_gui.GUIScreen;
import me.ihdeveloper.react_gui.std.GUIButton;
import me.ihdeveloper.react_gui.std.GUIImage;
import org.bukkit.Material;

import java.util.Map;
import java.util.TreeMap;

public class GUIPaginator extends GUIScreen {
    public static class Container {
        private final int columns;
        private final Map<Integer, GUIComponent> components;

        public Container(int columns) {
            this.columns = columns;
            components = new TreeMap<>();
        }

        public void setComponent(int x, int y, GUIComponent component) {
            int finalX = Math.max(1, Math.min(9, x));
            int finalY = Math.max(1, Math.min(columns, y));
            setComponent((9 * finalY) + finalX, component);
        }

        void setComponent(int index, GUIComponent component) {
            components.put(index, component);
        }
    }

    private final Container[] containers;

    public GUIPaginator(int pages, int columns, String title) {
        super(columns, title, true);

        this.containers = new Container[pages];

        for (int x = 1; x <= 9; x++) {
            if (x > pages) {
                setComponent(x, columns, new GUIImage("§ePage §c" + x, new String[] { "§7There's nothing here..." }, Material.STAINED_GLASS_PANE, 1, (short) 15));
                continue;
            }

            final int containerIndex = x;
            GUIButton button = new GUIButton("§ePage §6" + x, new String[] { "§7Click to visit this page" }, Material.STAINED_GLASS_PANE);
            button.setOnLeftClick((player) -> {
                Container container = getPage(containerIndex);
                renderOnScreen(container);
                return Unit.INSTANCE;
            });
            setComponent(x, columns, button);
        }
    }

    public void render() {
        renderOnScreen(getPage(1));
    }

    public Container getPage(int index) {
        index--;
        if (containers[index] == null) {
            containers[index] = new Container(getColumns() - 1);
        }
        return containers[index];
    }

    private void renderOnScreen(Container container) {
        for (int index = 1; index <= ((getColumns() - 1) * 9) - 1; index++) {
            setComponent(index, container.components.getOrDefault(index, null));
        }
    }
}
