package me.ihdeveloper.react.gui.test.screen;

import kotlin.Unit;
import me.ihdeveloper.react.gui.GUIContainer;
import me.ihdeveloper.react.gui.std.GUIButton;
import me.ihdeveloper.react.gui.std.GUICheckbox;
import me.ihdeveloper.react.gui.std.GUIFixedPaginator;
import me.ihdeveloper.react.gui.std.GUIImage;
import me.ihdeveloper.react.gui.std.GUIProgressBarPart;
import me.ihdeveloper.react.gui.std.GUIProgressGroup;
import me.ihdeveloper.react.gui.test.gui.ExpGroup;
import me.ihdeveloper.react.gui.test.gui.ExpManageComponent;
import org.bukkit.Material;

public class FixedInteractiveScreen extends GUIFixedPaginator {

    public FixedInteractiveScreen() {
        super(3, 5, "§8» Fixed Interactive Screen", true);

        GUIContainer firstPage = new GUIContainer();
        GUIContainer secondPage = new GUIContainer();
        GUIContainer thirdPage = new GUIContainer();
        GUIContainer fourthPage = new GUIContainer();

        /* First Page: Example */

        firstPage.setComponent(3, 2, new GUIImage("Image", new String[] { "A simple image" }, Material.CAKE));
        firstPage.setComponent(5, 2, new GUICheckbox(false, "Checkbox 1", new String[] { "§7A simple checkbox" }));

        /* Second Page: Another Example */

        secondPage.setComponent(3, 3, new GUIImage("Image", new String[] { "A simple image" }, Material.PUMPKIN_PIE));
        secondPage.setComponent(4, 3, new GUICheckbox(false, "Checkbox 2", new String[] { "§7A simple checkbox" }));

        /* Third Page: Experience Components  */

        ExpGroup expGroup = new ExpGroup();
        for (int x = 2; x <= 8; x++) {
            GUIProgressBarPart progressComponent = new GUIProgressBarPart();
            thirdPage.setComponent(x, 2, progressComponent);
            expGroup.add(progressComponent);
        }

        ExpManageComponent expManageComponent = new ExpManageComponent();
        thirdPage.setComponent(5, 3, expManageComponent);
        expGroup.setButton(expManageComponent);

        /* Fourth Page: Simple Progress Bar */
        final GUIProgressGroup progressGroup = new GUIProgressGroup(100, 20);

        for (int x = 3; x <= 7; x++) {
            GUIProgressBarPart part = new GUIProgressBarPart();
            fourthPage.setComponent(x, 2, part);
            progressGroup.add(part);
        }

        GUIButton progressButton = new GUIButton("§eManage §6Progress Bar", new String[] {
                "§7Manages the progress bar",
                "§7",
                "§eAdd §a+§65 §eto the bar §7(Left CLick)",
                "§cReset bar §7(Right Click)"
        }, Material.EXP_BOTTLE);
        progressButton.setOnLeftClick((player) -> {
            progressGroup.setCurrent(progressGroup.getCurrent() + 5);
            return Unit.INSTANCE;
        });
        progressButton.setOnRightClick((player) -> {
            progressGroup.setCurrent(0);
            return Unit.INSTANCE;
        });

        fourthPage.setComponent(5, 3, progressButton);

        /* Add the pages */
        addPage(firstPage);
        addPage(secondPage);
        addPage(thirdPage);
        addPage(fourthPage);

        /* Update the screen */
        update();
    }

}
