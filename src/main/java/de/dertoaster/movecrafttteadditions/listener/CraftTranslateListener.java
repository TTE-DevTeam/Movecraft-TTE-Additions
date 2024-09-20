package de.dertoaster.movecrafttteadditions.listener;

import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftDataTags;
import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftTypeProperties;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

import static org.apache.commons.lang3.reflect.FieldUtils.getField;

public class CraftTranslateListener implements Listener {

    protected static final Field ORIG_PILOT_TIME_FIELD = getField(BaseCraft.class, "origPilotTime");

    static {
        ORIG_PILOT_TIME_FIELD.setAccessible(true);
    }

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent event) {
        final Craft craft = event.getCraft();
        final CraftType type = craft.getType();
        if (craft.getCruising() && !(craft instanceof SinkingCraft)) {
            int armingDistance = type.getIntProperty(TTEAdditionsCraftTypeProperties.EXPLOSION_ARMING_DISTANCE);
            if (type.getFloatProperty(CraftType.COLLISION_EXPLOSION) > 0F && armingDistance > 0) {
                Location spawnPoint = craft.getDataTag(TTEAdditionsCraftDataTags.CRAFT_SPAWNPOINT);
                Location centerOfCraft = craft.getHitBox().getMidPoint().toBukkit(spawnPoint.getWorld());

                boolean distanceReached = false;
                if (spawnPoint.getWorld() != craft.getWorld()) {
                    distanceReached = true;
                } else {
                    distanceReached = spawnPoint.distanceSquared(centerOfCraft) > armingDistance;
                }

                if (distanceReached && craft instanceof BaseCraft baseCraft) {
                    try {
                        final long explosionTime = craft.getOrigPilotTime() + type.getIntProperty(CraftType.EXPLOSION_ARMING_TIME) + 1000;
                        ORIG_PILOT_TIME_FIELD.setLong(baseCraft, explosionTime);
                    } catch(Exception ex) {
                        // Ignore
                    }
                }
            }
        }
    }
}
