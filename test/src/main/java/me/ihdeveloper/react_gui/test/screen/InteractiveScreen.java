package me.ihdeveloper.react_gui.test.screen;

import me.ihdeveloper.react_gui.GUIContainer;
import me.ihdeveloper.react_gui.std.GUICheckbox;
import me.ihdeveloper.react_gui.std.GUIImage;
import me.ihdeveloper.react_gui.std.GUIPaginator;
import me.ihdeveloper.react_gui.test.gui.ExpGroup;
import me.ihdeveloper.react_gui.test.gui.ExpManageComponent;
import me.ihdeveloper.react_gui.test.gui.ExpProgressComponent;
import org.bukkit.Material;

public class InteractiveScreen extends GUIPaginator {

    public InteractiveScreen() {
        super(3, 5, "§8» Interactive Screen", true);

        GUIContainer firstPage = new GUIContainer();
        GUIContainer secondPage = new GUIContainer();
        GUIContainer thirdPage = new GUIContainer();

        /* First Page: Example */

        firstPage.setComponent(3, 2, new GUIImage("Image", new String[] { "A simple image" }, Material.CAKE));
        firstPage.setComponent(5, 2, new GUICheckbox(false, "Checkbox 1", new String[] { "§7A simple checkbox" }));

        /* Second Page: Another Example */

        secondPage.setComponent(3, 3, new GUIImage("Image", new String[] { "A simple image" }, Material.PUMPKIN_PIE));
        secondPage.setComponent(4, 3, new GUICheckbox(false, "Checkbox 2", new String[] { "§7A simple checkbox" }));

        /* Third Page: Experience Components  */

        ExpGroup expGroup = new ExpGroup();
        for (int x = 2; x <= 8; x++) {
            ExpProgressComponent progressComponent = new ExpProgressComponent();
            thirdPage.setComponent(x, 2, progressComponent);
            expGroup.getComponents().add(progressComponent);
        }

        ExpManageComponent expManageComponent = new ExpManageComponent();
        thirdPage.setComponent(5, 3, expManageComponent);
        expGroup.setButton(expManageComponent);

        /* Add the pages */
        addPage(firstPage);
        addPage(secondPage);
        addPage(thirdPage);

        /* Update the screen */
        update();
    }

}
