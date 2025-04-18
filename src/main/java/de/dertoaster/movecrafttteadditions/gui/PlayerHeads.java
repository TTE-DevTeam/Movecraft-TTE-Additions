package de.dertoaster.movecrafttteadditions.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class PlayerHeads {

    public static void setup() {
        // Initializes all the fields...
    }

    public static final ItemStack ARROW_LEFT = generateHeadItemStack(UUID.fromString("a68f0b64-8d14-4000-a95f-4b9ba14f8df9"), Component.text("<- Previous -"));
    public static final ItemStack ARROW_RIGHT = generateHeadItemStack(UUID.fromString("50c8510b-5ea0-4d60-be9a-7d542d6cd156"), Component.text("- Next ->"));

    private static ItemStack generateHeadItemStack(UUID ownerUUID, Component displayName) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID);
        final ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwningPlayer(offlinePlayer);
        meta.displayName(displayName);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

}
