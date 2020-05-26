package com.example.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Example plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Example plugin disabled!");
    }

}
