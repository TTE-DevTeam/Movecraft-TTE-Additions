package de.dertoaster.movecrafttteadditions.sign;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.SpeedSign;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class SpeedSignModified extends SpeedSign {

    @Override
    protected @Nullable Component getUpdateString(int lineIndex, Component oldData, Craft craft) {
        // TODO: Display the gear somewhere?
        switch(lineIndex) {
            case 1:
                return Component.text(String.format("%.2f",craft.getSpeed() / craft.getCruiseCooldownMultiplier()) + "m/s");
            case 2:
                return Component.text(String.format("%.2f",craft.getMeanCruiseTime() * 1000 * craft.getCruiseCooldownMultiplier()) + "ms");
            case 3:
                return Component.text(craft.getTickCooldown() * craft.getCruiseCooldownMultiplier() + "T");
            default:
                return null;
        }
    }
}
