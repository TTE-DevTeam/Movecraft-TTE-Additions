package de.dertoaster.movecrafttteadditions.commandrestrictor.listener;

import de.dertoaster.movecrafttteadditions.commandrestrictor.SignsOnCraftUtil;
import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftDataTags;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.features.status.events.CraftStatusUpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CraftDetectOrUpdateListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCraftDetect(final CraftDetectEvent event) {
        updateSignsOnCraft(event.getCraft());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCraftStatusUpdate(final CraftStatusUpdateEvent event) {
        updateSignsOnCraft(event.getCraft());
    }

    private static void updateSignsOnCraft(final Craft craft) {
        if (craft instanceof SinkingCraft || craft instanceof SubCraft) {
            return;
        }
        craft.setDataTag(TTEAdditionsCraftDataTags.SIGNS_ON_CRAFT, SignsOnCraftUtil.findCraftSignClassesOn(craft));
    }

}
