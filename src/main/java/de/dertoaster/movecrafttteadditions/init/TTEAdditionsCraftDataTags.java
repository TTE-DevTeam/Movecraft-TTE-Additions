package de.dertoaster.movecrafttteadditions.init;

import de.dertoaster.movecrafttteadditions.TTEAdditionsPlugin;
import net.countercraft.movecraft.craft.datatag.CraftDataTagKey;
import net.countercraft.movecraft.craft.datatag.CraftDataTagRegistry;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

public class TTEAdditionsCraftDataTags {

    public static final CraftDataTagKey<Location> CRAFT_SPAWNPOINT = CraftDataTagRegistry.INSTANCE.registerTagKey(new NamespacedKey(TTEAdditionsPlugin.INSTANCE, "craft_spawnpoint"), craft ->new Location(craft.getWorld(), craft.getHitBox().getMidPoint().getX(), craft.getHitBox().getMidPoint().getY(), craft.getHitBox().getMidPoint().getZ()));
    public static final CraftDataTagKey<Long> CRAFT_LAST_HONKED_AT = CraftDataTagRegistry.INSTANCE.registerTagKey(new NamespacedKey(TTEAdditionsPlugin.INSTANCE, "craft_last_honked_at"), craft -> null);

    public static void register() {
        // Does not need to do anything
    }

}
