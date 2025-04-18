package de.dertoaster.movecrafttteadditions;

import de.dertoaster.movecrafttteadditions.command.CraftTypeMergeInfoCommand;
import de.dertoaster.movecrafttteadditions.commandrestrictor.CommandRestriction;
import de.dertoaster.movecrafttteadditions.commandrestrictor.listener.CommandListener;
import de.dertoaster.movecrafttteadditions.commandrestrictor.listener.CraftDetectOrUpdateListener;
import de.dertoaster.movecrafttteadditions.gui.PlayerHeads;
import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftDataTags;
import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftTypeProperties;
import de.dertoaster.movecrafttteadditions.listener.CraftPilotListener;
import de.dertoaster.movecrafttteadditions.listener.CraftRotateListener;
import de.dertoaster.movecrafttteadditions.listener.CraftSinkListener;
import de.dertoaster.movecrafttteadditions.listener.CraftTranslateListener;
import de.dertoaster.movecrafttteadditions.sign.HonkSign;
import de.dertoaster.movecrafttteadditions.sign.IntegritySign;
import de.dertoaster.movecrafttteadditions.sign.ReverseCruiseSign;
import de.dertoaster.movecrafttteadditions.sign.SubcraftMoveSign;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.sign.MovecraftSignRegistry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class TTEAdditionsPlugin extends JavaPlugin {

    public static JavaPlugin INSTANCE;

    public static JavaPlugin getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        ConfigurationSerialization.registerClass(CommandRestriction.class, "CommandRestriction");

        saveDefaultConfig();

        // Call to init
        getConfig();

        TTEAdditionsCraftDataTags.register();

        // REgister commands
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            CraftTypeMergeInfoCommand.register(commands);
        });

        Bukkit.getServer().getPluginManager().registerEvents(new CraftPilotListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CraftTranslateListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CraftRotateListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CraftSinkListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CraftDetectOrUpdateListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CommandListener(), this);
        // Disabled until fixed
        //Bukkit.getServer().getPluginManager().registerEvents(new CraftDetectListener(), this);

        MovecraftSignRegistry.INSTANCE.register("Subcraft Move", new SubcraftMoveSign(CraftManager.getInstance()::getCraftTypeFromString, TTEAdditionsPlugin::getInstance), true, "SC Move");
        MovecraftSignRegistry.INSTANCE.register("Reverse:", new ReverseCruiseSign("Reverse:"));
        MovecraftSignRegistry.INSTANCE.register("Horn", new HonkSign());
        MovecraftSignRegistry.INSTANCE.register("Integrity:", new IntegritySign());

        PlayerHeads.setup();
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
