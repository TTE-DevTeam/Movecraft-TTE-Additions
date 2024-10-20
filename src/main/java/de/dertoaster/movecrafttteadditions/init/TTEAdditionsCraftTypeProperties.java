package de.dertoaster.movecrafttteadditions.init;

import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.BooleanProperty;
import net.countercraft.movecraft.craft.type.property.FloatProperty;
import net.countercraft.movecraft.craft.type.property.IntegerProperty;
import net.countercraft.movecraft.craft.type.property.StringProperty;
import org.bukkit.NamespacedKey;

public class TTEAdditionsCraftTypeProperties {

    public static final NamespacedKey EXPLOSION_ARMING_DISTANCE = new NamespacedKey("movecraft-tte-additions", "explosion_arming_distance");
    public static final NamespacedKey CRUISE_SIGNS_MUST_ALIGN = new NamespacedKey("movecraft-tte-additions", "cruise_signs_must_align");
    public static final NamespacedKey SKIP_CRUISE_SIGN_VALIDATION_WHEN_ON_CARRIER = new NamespacedKey("movecraft-tte-additions", "cruise_signs_must_align_skip_carrier");

    // Craft sounds!
    public static NamespacedKey SOUND_ON_ROTATION = new NamespacedKey("movecraft-tte-additions", "rotation_sound");

    public static NamespacedKey SOUND_ON_PILOT = new NamespacedKey("movecraft-tte-additions", "pilot_sound");
    public static NamespacedKey SOUND_ON_PILOT_PITCH_MIN = new NamespacedKey("movecraft-tte-additions", "pilot_sound_pitch_min");
    public static NamespacedKey SOUND_ON_PILOT_PITCH_MAX = new NamespacedKey("movecraft-tte-additions", "pilot_sound_pitch_max");


    public static NamespacedKey CAN_HONK = new NamespacedKey("movecraft-tte-additions", "can_honk");
    public static NamespacedKey HONK_SOUND = new NamespacedKey("movecraft-tte-additions", "honk_sound");
    public static NamespacedKey HONK_MIN_VOLUME = new NamespacedKey("movecraft-tte-additions", "honk_min_volume");
    public static NamespacedKey HONK_MAX_VOLUME = new NamespacedKey("movecraft-tte-additions", "honk_max_volume");
    public static NamespacedKey HONK_SIZE_SCALING = new NamespacedKey("movecraft-tte-additions", "honk_size_scaling");
    public static NamespacedKey HONK_SIZE_DIVISOR = new NamespacedKey("movecraft-tte-additions", "honk_size_divisor");
    public static NamespacedKey HONK_MAX_DISTANCE = new NamespacedKey("movecraft-tte-additions", "honk_max_distance");
    public static NamespacedKey HONK_MIN_COOLDOWN = new NamespacedKey("movecraft-tte-additions", "honk_min_cooldown");

    public static void register() {
        CraftType.registerProperty(new IntegerProperty("explosionArmingDistance", EXPLOSION_ARMING_DISTANCE, type -> -1));
        CraftType.registerProperty(new IntegerProperty("honkSizeDivisor", HONK_SIZE_DIVISOR, type -> 5000));
        CraftType.registerProperty(new IntegerProperty("honkMaxDistance", HONK_MAX_DISTANCE, type -> 256));
        CraftType.registerProperty(new IntegerProperty("honkMinCooldown", HONK_MIN_COOLDOWN, type -> 100));

        CraftType.registerProperty(new BooleanProperty("cruiseSignsMustAlign", CRUISE_SIGNS_MUST_ALIGN, type -> false));
        CraftType.registerProperty(new BooleanProperty("cruiseSignsMustAlignSkipCarrier", SKIP_CRUISE_SIGN_VALIDATION_WHEN_ON_CARRIER, type -> false));
        CraftType.registerProperty(new BooleanProperty("canHonk", CAN_HONK, type -> false));

        CraftType.registerProperty(new StringProperty("rotationSound", SOUND_ON_ROTATION, type -> null));
        CraftType.registerProperty(new StringProperty("pilotSound", SOUND_ON_PILOT, type -> null));
        CraftType.registerProperty(new StringProperty("honkSound", HONK_SOUND, type -> null));

        CraftType.registerProperty(new FloatProperty("pilotSoundPitchMin", SOUND_ON_PILOT_PITCH_MIN, type -> 0.9F));
        CraftType.registerProperty(new FloatProperty("pilotSoundPitchMax", SOUND_ON_PILOT_PITCH_MAX, type -> 1.1F));
        CraftType.registerProperty(new FloatProperty("honkMinVolume", HONK_MIN_VOLUME, type -> 1.0F));
        CraftType.registerProperty(new FloatProperty("honkMaxVolume", HONK_MAX_VOLUME, type -> 10.0F));
        CraftType.registerProperty(new FloatProperty("honkSizeScaling", HONK_SIZE_SCALING, type -> 0.1F));



    }

}
