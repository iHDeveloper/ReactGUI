package me.ihdeveloper.react_gui.test.screen;

import me.ihdeveloper.react_gui.std.GUICheckbox;
import me.ihdeveloper.react_gui.std.GUIImage;
import me.ihdeveloper.react_gui.test.GUIPaginator;
import org.bukkit.Material;

public class InteractiveScreen extends GUIPaginator {

    public InteractiveScreen() {
        super(2, 6, "§8» Interactive Screen");

        GUIPaginator.Container firstPage = getPage(1);
        firstPage.setComponent(3, 2, new GUIImage("Image", new String[] { "A simple image" }, Material.CAKE));
        firstPage.setComponent(5, 2, new GUICheckbox(false, "Checkbox 1", new String[] { "§7A simple checkbox" }));

        GUIPaginator.Container secondPage = getPage(2);
        secondPage.setComponent(3, 3, new GUIImage("Image", new String[] { "A simple image" }, Material.PUMPKIN_PIE));
        secondPage.setComponent(4, 3, new GUICheckbox(false, "Checkbox 2", new String[] { "§7A simple checkbox" }));

        render();
    }

}
