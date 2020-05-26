package me.ihdeveloper.spigot.starterkit;

public final class GradleStart {

    public static void main(String[] args) {
        String[] intro = {
                "   _____       _             __ ",
                "  / ___/____  (_)___ _____  / /_",
                "  \\__ \\/ __ \\/ / __ `/ __ \\/ __/",
                " ___/ / /_/ / / /_/ / /_/ / /_  ",
                "/____/ .___/_/\\__, /\\____/\\__/  ",
                "    /_/      /____/             ",
                "========"
        };
        for (String line : intro) {
            System.out.println(line);
        }
        System.out.println("> Spigot Starter Kit - By @iHDeveloper");
        System.out.println("> Starting debugging...");
        System.out.println();
        org.bukkit.craftbukkit.Main.main(args);
    }

}
