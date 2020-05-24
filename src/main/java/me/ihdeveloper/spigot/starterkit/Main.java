package me.ihdeveloper.spigot.starterkit;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Spigot starter kit enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Spigot starter kit disabled!");
    }

}
