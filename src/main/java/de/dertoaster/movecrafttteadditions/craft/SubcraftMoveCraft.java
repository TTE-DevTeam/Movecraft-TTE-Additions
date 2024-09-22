package de.dertoaster.movecrafttteadditions.craft;

import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubcraftMoveCraft extends BaseCraft implements PilotedCraft {

    private final Player pilot;

    public SubcraftMoveCraft(@NotNull CraftType type, @NotNull World world, @NotNull Player pilot) {
        super(type, world);
        this.pilot = pilot;
    }

    @NotNull
    @Override
    public Player getPilot() {
        return pilot;
    }
}
