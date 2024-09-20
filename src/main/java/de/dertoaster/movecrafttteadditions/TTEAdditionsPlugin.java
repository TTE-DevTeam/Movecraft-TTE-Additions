package de.dertoaster.movecrafttteadditions;

import org.bukkit.plugin.java.JavaPlugin;

public final class TTEAdditionsPlugin extends JavaPlugin {

    public static JavaPlugin INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
