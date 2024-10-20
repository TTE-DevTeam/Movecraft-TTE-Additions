package de.dertoaster.movecrafttteadditions.listener;

import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftDataTags;
import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftTypeProperties;
import de.dertoaster.movecrafttteadditions.util.MathUtil;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CraftPilotListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onCraftPilot(CraftPilotEvent event) {
        // Initializes the position
        event.getCraft().getDataTag(TTEAdditionsCraftDataTags.CRAFT_SPAWNPOINT);

        // Play sound if set
        final CraftType type = event.getCraft().getType();
        if (type.getStringProperty(TTEAdditionsCraftTypeProperties.SOUND_ON_PILOT) != null) {
            String sound = type.getStringProperty(TTEAdditionsCraftTypeProperties.SOUND_ON_PILOT);
            if (!sound.isBlank()) {
                Location location = event.getCraft().getDataTag(TTEAdditionsCraftDataTags.CRAFT_SPAWNPOINT);
                if (location != null) {
                    float settingMin = Math.abs(type.getFloatProperty(TTEAdditionsCraftTypeProperties.SOUND_ON_PILOT_PITCH_MIN));
                    float settingMax = Math.abs(type.getFloatProperty(TTEAdditionsCraftTypeProperties.SOUND_ON_PILOT_PITCH_MAX));
                    float minPitch = Math.max(0.0F, Math.min(settingMin, settingMax));
                    float maxPitch = Math.min(2.0F, Math.abs(Math.max(settingMin, settingMax)));
                    final float pitch = MathUtil.randomBetween(minPitch, maxPitch);
                    event.getCraft().getWorld().playSound(
                            location,
                            sound,
                            1,
                            pitch
                    );
                }
            }
        }
    }

}
