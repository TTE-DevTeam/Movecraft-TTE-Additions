package de.dertoaster.movecrafttteadditions.commandrestrictor.listener;

import de.dertoaster.movecrafttteadditions.commandrestrictor.CommandRestriction;
import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftTypeProperties;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.util.MathUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Set;

public class CommandListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Set<Craft> craftsInWorld = CraftManager.getInstance().getCraftsInWorld(player.getWorld());
        Craft playerCraft = CraftManager.getInstance().getCraftByPlayer(player);
        if (!((craftsInWorld != null && craftsInWorld.isEmpty()) || playerCraft != null)) {
            return;
        }
        Craft craftToUse = playerCraft;
        if (playerCraft != null) {
            craftToUse = (Craft) MathUtils.craftsNearLocFast(craftsInWorld, player.getLocation()).toArray()[0];
        }
        // Unnecessary, but doesnt really hurt
        if (craftToUse == null) {
            return;
        }

        if (!craftToUse.getType().getBoolProperty(TTEAdditionsCraftTypeProperties.COMMAND_RESTRICTIONS_ENABLED)) {
            return;
        }

        final String command = event.getMessage().split(" ")[0];

        List<CommandRestriction> restrictions = CommandRestriction.COMMAND_TO_RESTRICTION.get(command);
        if (restrictions == null || restrictions.isEmpty()) {
            return;
        }
        for (CommandRestriction restriction : restrictions) {
            if (restriction.restricts(event.getMessage(), craftToUse)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("Required sign not found aboard craft!"));
                return;
            }
        }
    }

}
