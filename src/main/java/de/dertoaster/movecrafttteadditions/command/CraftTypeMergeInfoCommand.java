package de.dertoaster.movecrafttteadditions.command;

import com.mojang.brigadier.Command;
import de.dertoaster.movecrafttteadditions.command.argument.type.CraftTypeArgumentType;
import de.dertoaster.movecrafttteadditions.command.argument.type.EnumArgumentType;
import de.dertoaster.movecrafttteadditions.gui.BackButton;
import de.dertoaster.movecrafttteadditions.gui.ForwardButton;
import de.dertoaster.movecrafttteadditions.gui.Layouts;
import io.papermc.paper.command.brigadier.Commands;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CraftTypeMergeInfoCommand {

    enum JOIN_MODE {
        INCLUDES,
        EXCLUDES
    }

    public static void register(final Commands commands) {
        commands.register(
            Commands.literal("crafttypeinfo")
                .requires(source -> source.getSender() instanceof Player)
                .requires(source -> source.getSender().hasPermission("tteadditions.command.crafttypeinfo"))
                .executes(commandSourceStack -> {
                    commandSourceStack.getSource().getSender().sendMessage(Component.text("At least one crafttype must be given!"));
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("crafttypeA", new CraftTypeArgumentType())
                    .executes(context -> {
                        CraftType argProvided = context.getArgument("crafttypeA", CraftType.class);
                        if (argProvided == null) {
                            context.getSource().getSender().sendMessage(Component.text("Invalid crafttype <" + argProvided + ">!", Style.style(TextColor.color(1.0F, 0.0F, 0.0F))));
                        } else {
                            process((Player)(context.getSource().getSender()), argProvided);
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(Commands.argument("jointype", EnumArgumentType.<JOIN_MODE>ofEnum(JOIN_MODE.class))
                        .executes(context -> {
                            JOIN_MODE argProvided = context.getArgument("jointype", JOIN_MODE.class);
                            if (argProvided == null) {
                                context.getSource().getSender().sendMessage(Component.text("Invalid join operation <" + argProvided + ">!", Style.style(TextColor.color(1.0F, 0.0F, 0.0F))));
                            } else {
                                process((Player) context.getSource().getSender(), CraftManager.getInstance().getCraftTypeFromString(context.getArgument("crafttypeA", String.class)));
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("crafttypeB", new CraftTypeArgumentType())
                            .executes(context -> {
                                CraftType argProvided = context.getArgument("crafttypeB", CraftType.class);
                                if (argProvided == null) {
                                    context.getSource().getSender().sendMessage(Component.text("Invalid crafttype <" + argProvided + ">!", Style.style(TextColor.color(1.0F, 0.0F, 0.0F))));
                                } else {
                                    process((Player)(context.getSource().getSender()),  context.getArgument("crafttypeA", CraftType.class), context.getArgument("jointype", JOIN_MODE.class), context.getArgument("crafttypeB", CraftType.class));
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                )
                .build(),
                "Command to help with finding out what merges to certain crafttypes",
                List.of("cti", "ctinfo")
        );
    }

    static void process(Player sender, CraftType craftTypeA, JOIN_MODE jointype, CraftType craftTypeB) {
        Set<Material> matSetA = new HashSet<>();
        matSetA.addAll(craftTypeA.getMaterialSetProperty(CraftType.ALLOWED_BLOCKS));
        Set<Material> matSetB = new HashSet<>();
        matSetB.addAll(craftTypeB.getMaterialSetProperty(CraftType.ALLOWED_BLOCKS));

        Set<Material> matSet = new HashSet<>();
        matSet.addAll(matSetA);

        Component heading;

        if (jointype == JOIN_MODE.INCLUDES) {
            matSet.removeIf(m -> !matSetB.contains(m));
            heading = Component.text("Blocks of: " + craftTypeA.getStringProperty(CraftType.NAME) + " AND " + craftTypeB.getStringProperty(CraftType.NAME)).style(Style.style(TextColor.color(0F, 1F, 0F)));
        } else {
            matSet.removeAll(matSetB);
            heading = Component.text("Blocks of: " + craftTypeA.getStringProperty(CraftType.NAME) + " AND NOT " + craftTypeB.getStringProperty(CraftType.NAME)).style(Style.style(TextColor.color(0F, 1F, 0F)));
        }

        process(sender, matSet, heading);
    }

    static void process(Player sender, CraftType craftType) {
        Component heading = Component.text("Blocks of: " + craftType.getStringProperty(CraftType.NAME)).style(Style.style(TextColor.color(0F, 1F, 0F)));
        Set<Material> matSet = new HashSet<>();
        for (Material mat : craftType.getMaterialSetProperty(CraftType.ALLOWED_BLOCKS)) {
            matSet.add(mat);
        }
        process(sender, matSet, heading);
    }

    static void process(Player sender, Set<Material> materialSet, Component heading) {
        // Now, create the GUI!
        List<Item> items = materialSet.stream()
                .filter(material -> material.isItem())
                .map(material -> new SimpleItem(new ItemBuilder(material)))
                .sorted(Comparator.comparing(a -> a.getItemProvider().get().getType().name()))
                .collect(Collectors.toUnmodifiableList());

        Gui gui = PagedGui.items()
                .setStructure(Layouts.PAGED_WITH_TITLE_6_ROWS)
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("")))
                .addIngredient('<', new BackButton())
                .addIngredient('>', new ForwardButton())
                .setContent(items)
                .build();

        // TODO: Include search functionality by using a anvil GUI
        Window.single()
                .setGui(gui)
                .setTitle(new AdventureComponentWrapper(heading))
                .open(sender);
    }

}
