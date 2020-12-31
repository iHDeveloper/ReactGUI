package me.ihdeveloper.react_gui.test.screen;

import me.ihdeveloper.react_gui.std.GUICheckbox;
import me.ihdeveloper.react_gui.std.GUIImage;
import me.ihdeveloper.react_gui.test.GUIPaginator;
import me.ihdeveloper.react_gui.test.gui.ExpGroup;
import me.ihdeveloper.react_gui.test.gui.ExpManageComponent;
import me.ihdeveloper.react_gui.test.gui.ExpProgressComponent;
import org.bukkit.Material;

public class InteractiveScreen extends GUIPaginator {

    public InteractiveScreen() {
        super(3, 5, "§8» Interactive Screen");

        /* First Page: Example */

        GUIPaginator.Container firstPage = getPage(1);
        firstPage.setComponent(3, 2, new GUIImage("Image", new String[] { "A simple image" }, Material.CAKE));
        firstPage.setComponent(5, 2, new GUICheckbox(false, "Checkbox 1", new String[] { "§7A simple checkbox" }));

        /* Second Page: Another Example */

        GUIPaginator.Container secondPage = getPage(2);
        secondPage.setComponent(3, 3, new GUIImage("Image", new String[] { "A simple image" }, Material.PUMPKIN_PIE));
        secondPage.setComponent(4, 3, new GUICheckbox(false, "Checkbox 2", new String[] { "§7A simple checkbox" }));

        /* Third Page: Experience Components  */

        ExpGroup expGroup = new ExpGroup();

        GUIPaginator.Container thirdPage = getPage(3);

        for (int x = 2; x <= 8; x++) {
            ExpProgressComponent progressComponent = new ExpProgressComponent();
            thirdPage.setComponent(x, 2, progressComponent);
            expGroup.getComponents().add(progressComponent);
        }

        ExpManageComponent expManageComponent = new ExpManageComponent();
        thirdPage.setComponent(5, 3, expManageComponent);
        expGroup.setButton(expManageComponent);

        /* Renders the first page in the screen */
        render();
    }

}
