package de.dertoaster.movecrafttteadditions.commandrestrictor;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.AbstractMovecraftSign;
import net.countercraft.movecraft.sign.MovecraftSignRegistry;
import net.countercraft.movecraft.sign.SignListener;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.util.HashSet;
import java.util.Set;

public class SignsOnCraftUtil {

    public static Set<Class<? extends AbstractMovecraftSign>> findCraftSignClassesOn(final Craft craft) {
        Set<Class<? extends AbstractMovecraftSign>> result = new HashSet<>();

        for (MovecraftLocation movecraftLocation : craft.getHitBox()) {
            Block block = movecraftLocation.toBukkit(craft.getWorld()).getBlock();

            BlockState state = block.getState();
            if (!(state instanceof Sign)) {
                continue;
            }
            Sign sign = (Sign)state;
            SignListener.SignWrapper[] wrappers = SignListener.INSTANCE.getSignWrappers(sign, true);
            for (SignListener.SignWrapper wrapper : wrappers) {
                AbstractMovecraftSign signHandler = MovecraftSignRegistry.INSTANCE.get(wrapper.line(0));
                if (signHandler == null)
                    continue;
                result.add(signHandler.getClass());
            }

        }

        return result;
    }

}
