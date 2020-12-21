package me.ihdeveloper.react_gui.test;

import kotlin.Unit;
import me.ihdeveloper.react_gui.GUIScreen;
import me.ihdeveloper.react_gui.GUIScreenListener;
import me.ihdeveloper.react_gui.ReactGUI;
import me.ihdeveloper.react_gui.std.GUIButton;
import me.ihdeveloper.react_gui.std.GUICheckbox;
import me.ihdeveloper.react_gui.std.GUIImage;
import me.ihdeveloper.react_gui.std.GUIRadioGroup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JavaGUIScreen extends GUIScreen implements GUIScreenListener {
    private final GUICheckbox perm1 = new GUICheckbox(false, "Permission 1", new String[] { "Description for permission 1" });
    private final GUICheckbox perm2 = new GUICheckbox(false, "Permission 2", new String[] { "Description for permission 2" });
    private final GUICheckbox perm3 = new GUICheckbox(false, "Permission 3", new String[] { "Description for permission 3" });

    private final GUIRadioGroup modeGroup = new GUIRadioGroup(false);
    private final GUICheckbox mode1 = new GUICheckbox(false, "Mode 1", new String[] { "Description for mode 1" });
    private final GUICheckbox mode2 = new GUICheckbox(false, "Mode 2", new String[] { "Description for mode 2" });
    private final GUICheckbox mode3 = new GUICheckbox(false, "Mode 3", new String[] { "Description for mode 3" });

    public JavaGUIScreen() {
        super(6, "§1» §9Screen written with §3Java", true);

        setComponent(3, 2, new GUIImage("Permission 1", new String[0], Material.WHEAT));
        setComponent(3, 3, perm1);
        setComponent(5, 2, new GUIImage("Permission 2", new String[0], Material.COAL));
        setComponent(5, 3, perm2);
        setComponent(7, 2, new GUIImage("Permission 3", new String[0], Material.IRON_INGOT));
        setComponent(7, 3, perm3);

        setComponent(2, 5, new GUIImage("Mode 1", new String[0], Material.SIGN, 1, (short) 0));
        setComponent(2, 6, mode1);
        setComponent(3, 5, new GUIImage("Mode 2", new String[0], Material.CLAY_BALL, 1, (short) 0));
        setComponent(3, 6, mode2);
        setComponent(4, 5, new GUIImage("Mode 3", new String[0], Material.DIAMOND, 1, (short) 0));
        setComponent(4, 6, mode3);

        GUIButton closeButton = new GUIButton("§cClose Screen", new String[]{"§7Click to close"}, Material.ARROW);
        closeButton.setHandler((player) -> {
            ReactGUI.closeScreen(player);
            return Unit.INSTANCE;
        });
        setComponent(9, 6, closeButton);

        setEventHandler(this);
    }

    @Override
    public void onOpen(@NotNull Player player) {
        /* This method is being called before opening the inventory (aka Screen) */

        // TODO load from mysql and update the state of the components
        perm1.setChecked(true);
        perm2.setChecked(false);
        perm3.setChecked(true);

        mode1.setChecked(true);
        mode2.setChecked(false);
        mode3.setChecked(false);

        /* Radio group helps forces the player to choose only one option! */
        modeGroup.add(mode1);
        modeGroup.add(mode2);
        modeGroup.add(mode3);
    }

    @Override
    public void onClose(@NotNull Player player, boolean forced) {
        modeGroup.clear();

        if (forced) {
            Bukkit.getConsoleSender().sendMessage("§eCouldn't update the data for player §9" + player.getName() + " §7(FORCED_CLOSE)");
            return;
        }

        boolean p1 = perm1.isChecked();
        boolean p2 = perm2.isChecked();
        boolean p3 = perm3.isChecked();

        boolean m1 = mode1.isChecked();
        boolean m2 = mode2.isChecked();
        boolean m3 = mode3.isChecked();

        player.sendMessage("§ePermission 1:§" + (p1 ? "a true" : "c false"));
        player.sendMessage("§ePermission 2:§" + (p2 ? "a true" : "c false"));
        player.sendMessage("§ePermission 3:§" + (p3 ? "a true" : "c false"));

        StringBuilder builder = new StringBuilder();
        builder.append("§eYou have chosen mode §7(not required)§e:§");
        if (m1) {
            builder.append("f Mode 1");
        } else if (m2) {
            builder.append("f Mode 2");
        } else if (m3) {
            builder.append("f Mode 3");
        } else {
            builder.append("c None");
        }
        player.sendMessage(builder.toString());
    }
}
