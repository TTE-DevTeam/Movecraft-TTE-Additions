package de.dertoaster.movecrafttteadditions;

import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftDataTags;
import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftTypeProperties;
import de.dertoaster.movecrafttteadditions.listener.CraftDetectListener;
import de.dertoaster.movecrafttteadditions.listener.CraftPilotListener;
import de.dertoaster.movecrafttteadditions.listener.CraftRotateListener;
import de.dertoaster.movecrafttteadditions.listener.CraftTranslateListener;
import de.dertoaster.movecrafttteadditions.sign.*;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.listener.CraftTypeListener;
import net.countercraft.movecraft.sign.MovecraftSignRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TTEAdditionsPlugin extends JavaPlugin {

    public static JavaPlugin INSTANCE;

    public static JavaPlugin getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        TTEAdditionsCraftDataTags.register();

        Bukkit.getServer().getPluginManager().registerEvents(new CraftPilotListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CraftTranslateListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CraftRotateListener(), this);
        // Disabled until fixed
        //Bukkit.getServer().getPluginManager().registerEvents(new CraftDetectListener(), this);

        MovecraftSignRegistry.INSTANCE.register("Subcraft Move", new SubcraftMoveSign(CraftManager.getInstance()::getCraftTypeFromString, TTEAdditionsPlugin::getInstance), true, "SC Move");
        MovecraftSignRegistry.INSTANCE.register("Reverse:", new ReverseCruiseSign("Reverse:"));
        MovecraftSignRegistry.INSTANCE.register("Speed:", new SpeedSignModified(), true);
        MovecraftSignRegistry.INSTANCE.register("Horn", new HonkSign());
        MovecraftSignRegistry.INSTANCE.register("Integrity:", new IntegritySign());
    }

    @Override
    public void onLoad() {
        TTEAdditionsCraftTypeProperties.register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
