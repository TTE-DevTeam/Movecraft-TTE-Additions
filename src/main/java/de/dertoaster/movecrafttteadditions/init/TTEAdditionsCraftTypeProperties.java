package de.dertoaster.movecrafttteadditions.init;

import de.dertoaster.movecrafttteadditions.TTEAdditionsPlugin;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.IntegerProperty;
import org.bukkit.NamespacedKey;

public class TTEAdditionsCraftTypeProperties {

    public static final NamespacedKey EXPLOSION_ARMING_DISTANCE = new NamespacedKey(TTEAdditionsPlugin.INSTANCE, "explosion_arming_distance");

    public static void register() {
        CraftType.registerProperty(new IntegerProperty("explosionArmingDistance", EXPLOSION_ARMING_DISTANCE, type -> -1));
    }

}
