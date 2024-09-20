package de.dertoaster.movecrafttteadditions.sign;

import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.sign.SubcraftRotateSign;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class SubcraftRollSign extends SubcraftRotateSign {
    public SubcraftRollSign(Function<String, @Nullable CraftType> craftTypeRetrievalFunction, Supplier<Plugin> plugin) {
        super(craftTypeRetrievalFunction, plugin);
    }
}
