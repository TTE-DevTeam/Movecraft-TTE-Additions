package de.dertoaster.movecrafttteadditions.init;

import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.BooleanProperty;
import net.countercraft.movecraft.craft.type.property.IntegerProperty;
import org.bukkit.NamespacedKey;

public class TTEAdditionsCraftTypeProperties {

    public static final NamespacedKey EXPLOSION_ARMING_DISTANCE = new NamespacedKey("movecraft-tte-additions", "explosion_arming_distance");
    public static final NamespacedKey CRUISE_SIGNS_MUST_ALIGN = new NamespacedKey("movecraft-tte-additions", "cruise_signs_must_align");
    public static final NamespacedKey SKIP_CRUISE_SIGN_VALIDATION_WHEN_ON_CARRIER = new NamespacedKey("movecraft-tte-additions", "cruise_signs_must_align_skip_carrier");

    public static void register() {
        CraftType.registerProperty(new IntegerProperty("explosionArmingDistance", EXPLOSION_ARMING_DISTANCE, type -> -1));
        CraftType.registerProperty(new BooleanProperty("cruiseSignsMustAlign", CRUISE_SIGNS_MUST_ALIGN, type -> false));
        CraftType.registerProperty(new BooleanProperty("cruiseSignsMustAlignSkipCarrier", SKIP_CRUISE_SIGN_VALIDATION_WHEN_ON_CARRIER, type -> false));
    }

}
