package de.dertoaster.movecrafttteadditions.listener;

import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftTypeProperties;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftSinkEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CraftSinkListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCraftSink(final CraftSinkEvent event) {
        final CraftType type = event.getCraft().getType();

        final String sound = type.getStringProperty(TTEAdditionsCraftTypeProperties.SOUND_ON_SINK);
        if (sound == null || sound.isBlank()) {
            return;
        }

        final World world = event.getCraft().getWorld();
        if (world == null) {
            return;
        }
        final Location position = event.getCraft().getHitBox().getMidPoint().toBukkit(world);

        world.playSound(position, sound, 1, 1);
    }

}