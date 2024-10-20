package de.dertoaster.movecrafttteadditions.sign;

import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftDataTags;
import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftTypeProperties;
import de.dertoaster.movecrafttteadditions.util.MathUtil;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.sign.AbstractCraftSign;
import net.countercraft.movecraft.sign.SignListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class HonkSign extends AbstractCraftSign {

    // Format: Honk
    // Pitch
    // Cooldown

    public HonkSign() {
        super("movecraft.honksign", true);
    }

    @Override
    protected void onCraftIsBusy(Player player, Craft craft) {
        // Ignore
    }

    @Override
    protected void onCraftNotFound(Player player, SignListener.SignWrapper signWrapper) {
        // Show error
    }

    @Override
    protected boolean internalProcessSignWithCraft(Action action, SignListener.SignWrapper signWrapper, Craft craft, Player player) {
        final CraftType type = craft.getType();

        final int effectiveCooldown = extractCooldown(signWrapper, type);
        if (isCooldownActive(craft, effectiveCooldown)) {
            return false;
        }

        // FIRST: Set the last honk point
        craft.setDataTag(TTEAdditionsCraftDataTags.CRAFT_LAST_HONKED_AT, craft.getWorld().getGameTime());

        final float pitch = extractPitch(signWrapper, type);
        final int maxDistance = type.getIntProperty(TTEAdditionsCraftTypeProperties.HONK_MAX_DISTANCE);
        final float minVolume = type.getFloatProperty(TTEAdditionsCraftTypeProperties.HONK_MIN_VOLUME);
        final float sizeScaling = type.getFloatProperty(TTEAdditionsCraftTypeProperties.HONK_SIZE_SCALING);
        final int sizeDivisor = type.getIntProperty(TTEAdditionsCraftTypeProperties.HONK_SIZE_DIVISOR);
        final float sizeMultiplier = (craft.getHitBox().size() / sizeDivisor) * sizeScaling;
        final float maxVolume = Math.min(minVolume + sizeMultiplier, type.getFloatProperty(TTEAdditionsCraftTypeProperties.HONK_MAX_VOLUME));
        final float volumeRange = maxVolume - minVolume;
        final String sound = type.getStringProperty(TTEAdditionsCraftTypeProperties.HONK_SOUND);
        final Location centerLocation = craft.getHitBox().getMidPoint().toBukkit(craft.getWorld());

        for (Player playerTmp : craft.getWorld().getNearbyPlayers(centerLocation, maxDistance)) {
            double distancePercentage = 1.0D - (Math.min(maxDistance, playerTmp.getLocation().distance(centerLocation)) / maxDistance);
            final float effectiveVolume = minVolume + (float)(distancePercentage * volumeRange);
            // Offset the sound a bit so the player knows where its coming from
            Vector offset = centerLocation.toVector().subtract(playerTmp.getLocation().toVector());
            offset = offset.normalize().multiply(8);
            playerTmp.playSound(playerTmp.getLocation().add(offset), sound, effectiveVolume, pitch);
        }

        return true;
    }

    @Override
    protected boolean isSignValid(Action action, SignListener.SignWrapper signWrapper, Player player) {
        String rawPitches = signWrapper.getRaw(1);
        if (rawPitches != null && !rawPitches.isBlank()) {
            String[] pitches = rawPitches.split(",");
            if (pitches.length == 1) {
                try {
                    Float.parseFloat(pitches[0]);
                } catch(NumberFormatException nfe) {
                    player.sendMessage(I18nSupport.getInternationalisedComponent("<" + pitches[0] + "> is not a valid float number!"));
                    return false;
                }
            } else if (pitches.length == 2) {
                for (String s : pitches) {
                    try {
                        Float.parseFloat(s);
                    } catch(NumberFormatException nfe) {
                        player.sendMessage(I18nSupport.getInternationalisedComponent("<" + s + "> is not a valid float number!"));
                        return false;
                    }
                }
            } else {
                player.sendMessage(I18nSupport.getInternationalisedComponent("Either 0, 1 or 2 pitch values can be set!"));
                return false;
            }
        }

        String rawCooldown = signWrapper.getRaw(2);
        if (rawCooldown != null && !rawCooldown.isBlank()) {
            int cooldownSignValue = -1;
            rawCooldown = rawCooldown.trim();
            try {
                cooldownSignValue = Integer.parseInt(rawCooldown);
            } catch(NumberFormatException nfe) {
                player.sendMessage(I18nSupport.getInternationalisedComponent("<" + rawCooldown + "> is not a integer value!"));
                return false;
            }
            if (cooldownSignValue < 0) {
                player.sendMessage(I18nSupport.getInternationalisedComponent("Cooldown can not be negative!"));
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean processSignChange(SignChangeEvent signChangeEvent, SignListener.SignWrapper signWrapper) {
        // Ignore
        return true;
    }

    static float extractPitch(final SignListener.SignWrapper wrapper, final CraftType craftType) {
        String raw = wrapper.getRaw(2);
        float pitchMin = 1.0F;
        float pitchMax = 1.0F;
        if (raw != null && !raw.isBlank()) {
            raw = raw.trim();
            try {
                String[] floats = wrapper.getRaw(1).split(",");
                if (floats.length >= 2) {
                    pitchMin = Float.parseFloat(floats[0]);
                    pitchMax = Float.parseFloat(floats[1]);
                } else {
                    float value = Float.parseFloat(floats[0]);
                    pitchMin = value;
                    pitchMax = value;
                }
            } catch(NumberFormatException nfe) {
                // Ignore
                pitchMin = 1.0F;
                pitchMax = 1.0F;
            }
        }
        return MathUtil.randomBetween(pitchMin, pitchMax);
    }

    static int extractCooldown(final SignListener.SignWrapper wrapper, final CraftType craftType) {
        String raw = wrapper.getRaw(2);
        final int cooldownMin = craftType.getIntProperty(TTEAdditionsCraftTypeProperties.HONK_MIN_COOLDOWN);
        int cooldown = cooldownMin;
        if (raw != null && !raw.isBlank()) {
            try {
                cooldown = Math.max(Math.abs(Integer.parseInt(raw.trim())), cooldownMin);
            } catch(NumberFormatException nfe) {
                // Ignore
                cooldown = cooldownMin;
            }
        }
        return cooldown;
    }

    @Override
    protected boolean canPlayerUseSignOn(Player player, @Nullable Craft craft) {
        if (super.canPlayerUseSignOn(player, craft)) {
            if (!craft.getType().getBoolProperty(TTEAdditionsCraftTypeProperties.CAN_HONK)) {
                player.sendMessage(I18nSupport.getInternationalisedComponent("This craft can't honk :("));
                return false;
            }
            String honkSound = craft.getType().getStringProperty(TTEAdditionsCraftTypeProperties.HONK_SOUND);
            if (honkSound == null || honkSound.isBlank()) {
                return false;
            }
            final int cooldown = craft.getType().getIntProperty(TTEAdditionsCraftTypeProperties.HONK_MIN_COOLDOWN);
            if (isCooldownActive(craft, cooldown)) {
                return false;
            }
            return true;
        }
        return false;
    }

    static boolean isCooldownActive(final Craft craft, final int effectiveCooldown) {
        Long lastHonkHonk = craft.getDataTag(TTEAdditionsCraftDataTags.CRAFT_LAST_HONKED_AT);
        if (lastHonkHonk != null) {
            final long worldTicks = craft.getWorld().getGameTime();
            if (worldTicks - lastHonkHonk <= effectiveCooldown) {
                return true;
            }
        }
        return false;
    }
}
