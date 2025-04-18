package de.dertoaster.movecrafttteadditions.sign;

import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.CruiseSign;
import net.countercraft.movecraft.sign.SignListener;
import org.bukkit.entity.Player;

public class ReverseCruiseSign extends CruiseSign {

    public ReverseCruiseSign(String ident) {
        super(ident);
    }

    @Override
    protected CruiseDirection getCruiseDirection(SignListener.SignWrapper sign) {
        return super.getCruiseDirection(sign).getOpposite2D();
    }

    @Override
    protected void setCraftCruising(Player player, CruiseDirection direction, Craft craft) {
        super.setCraftCruising(player, direction, craft);
        // TODO: Add speed multiplier for crafts
        craft.setCruiseCooldownMultiplier(2D);
    }
}
