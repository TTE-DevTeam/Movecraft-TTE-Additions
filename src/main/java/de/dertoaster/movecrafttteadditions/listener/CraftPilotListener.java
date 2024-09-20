package de.dertoaster.movecrafttteadditions.listener;

import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftDataTags;
import net.countercraft.movecraft.events.CraftPilotEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CraftPilotListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onCraftPilot(CraftPilotEvent event) {
        // Initializes the position
        event.getCraft().getDataTag(TTEAdditionsCraftDataTags.CRAFT_SPAWNPOINT);
    }
}
