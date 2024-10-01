package de.dertoaster.movecrafttteadditions.listener;

import de.dertoaster.movecrafttteadditions.TTEAdditionsPlugin;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.SubCraftImpl;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.processing.functions.Result;
import net.countercraft.movecraft.sign.*;
import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftTypeProperties.CRUISE_SIGNS_MUST_ALIGN;
import static de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftTypeProperties.SKIP_CRUISE_SIGN_VALIDATION_WHEN_ON_CARRIER;

public class CraftDetectListener implements Listener {

    static final List<MovecraftLocation> STARTING_LOCATIONS_IN_USE = Collections.synchronizedList(new ArrayList<>());

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCraftDetect(final CraftDetectEvent event) {

        if (event.getCraft() instanceof SubCraft) {
            return;
        }

        if (STARTING_LOCATIONS_IN_USE.contains(event.getStartLocation())) {
            return;
        }

        final CraftType type = event.getCraft().getType();
        final Craft craft = event.getCraft();

        if (!type.getBoolProperty(CRUISE_SIGNS_MUST_ALIGN)) {
            return;
        }

        final List<MovecraftLocation> pilotSignLocations = Collections.synchronizedList(new ArrayList<>());
        final Map<MovecraftLocation, List<SignListener.SignWrapper>> wrapperLocationMap = Object2ObjectMaps.synchronize(new Object2ObjectArrayMap<>());
        final Function<MovecraftLocation, List<SignListener.SignWrapper>> wrapperRetrievalFunction = wrapperLocationMap::get;
        final Map<Class<? extends CruiseSign>, Set<SignListener.SignWrapper>> signMap = Object2ObjectMaps.synchronize(new Object2ObjectArrayMap<>());
        final BiConsumer<Class<? extends CruiseSign>, SignListener.SignWrapper> removalFunction = (c, s) -> {
            Set<SignListener.SignWrapper> wrappers = signMap.getOrDefault(c, null);
            if (wrappers != null && s != null) {
                wrappers.remove(s);
            }
        };
        final Map<CraftType, List<MovecraftLocation>> pilotSigns = new Object2ObjectArrayMap<>();
        // Find all signs
        for (MovecraftLocation mLoc : craft.getHitBox()) {
            if (mLoc == event.getStartLocation()) {
                continue;
            }
            Block block = mLoc.toBukkit(craft.getWorld()).getBlock();
            BlockState state = block.getState();
            if (!(state instanceof Sign)) {
                continue;
            }
            Sign sign = (Sign)state;
            SignListener.SignWrapper[] wrappers = SignListener.INSTANCE.getSignWrappers(sign, true);
            List<SignListener.SignWrapper> wrappersForMapping = new ArrayList<>();
            for (SignListener.SignWrapper wrapper : wrappers) {
                AbstractMovecraftSign signHandler = MovecraftSignRegistry.INSTANCE.get(wrapper.line(0));
                if (signHandler instanceof CruiseSign cruiseSign) {
                    signMap.computeIfAbsent(cruiseSign.getClass(), c -> new ObjectArraySet<>()).add(wrapper);
                    wrappersForMapping.add(wrapper);
                } else if (signHandler instanceof CraftPilotSign pilotSign) {
                    if (pilotSign.getCraftType() != null && !pilotSign.getCraftType().getBoolProperty(CraftType.CRUISE_ON_PILOT)) {
                        pilotSignLocations.add(mLoc);
                        pilotSigns.computeIfAbsent(pilotSign.getCraftType(), t -> new ArrayList<>()).add(mLoc);
                    }
                }
            }
            if (!wrappersForMapping.isEmpty()) {
                wrapperLocationMap.put(mLoc, wrappersForMapping);
            }
        }
        // Then iterate through all craft signs => Remove the entries we no longer need
        pilotSigns.forEach((typeTmp, points) -> {
            // If we don't care about the cruise signs when on a carrier, we need to remove them
            // TODO: We can't do it like this!
            // This will cause a infinite loop cause detection triggers the event again
            // Also detection happens async...
            if (!typeTmp.getBoolProperty(SKIP_CRUISE_SIGN_VALIDATION_WHEN_ON_CARRIER)) {
                for (MovecraftLocation startingPoint : points) {
                    CraftManager.getInstance().detect(
                            startingPoint,
                            typeTmp,
                            (typeTmpTmp, w, p, parents) -> {
                                if (parents.size() > 1)
                                    return new Pair<>(Result.fail(), null);

                                return new Pair<>(Result.succeed(), new SubCraftImpl(typeTmpTmp, w, craft));
                            },
                            craft.getWorld(),
                            null,
                            Audience.empty(),
                            (Craft subcraft) -> () -> removeSignsAndReAddToParent(subcraft, craft, removalFunction, wrapperRetrievalFunction)
                    );
                }
            }
        });
        final Craft parentCraft = event.getCraft();
        // Now, everything is filtered, check for more than one cruise direction
        Bukkit.getScheduler().runTaskLater(TTEAdditionsPlugin.INSTANCE, () -> {
            STARTING_LOCATIONS_IN_USE.removeAll(pilotSignLocations);
            boolean duplicateFound = false;
            for (Map.Entry<Class<? extends CruiseSign>, Set<SignListener.SignWrapper>> entry : signMap.entrySet()) {
                CruiseDirection direction = CruiseDirection.NONE;
                for (SignListener.SignWrapper wrapper : entry.getValue()) {
                    if (duplicateFound) {
                        break;
                    }
                    if (direction == CruiseDirection.NONE) {
                        direction = CruiseDirection.fromBlockFace(wrapper.facing());
                    } else {
                        duplicateFound = direction != CruiseDirection.fromBlockFace(wrapper.facing());
                    }
                }
                if (duplicateFound) {
                    break;
                }
            };

            if (duplicateFound) {
                parentCraft.getAudience().sendMessage(Component.text("Detection failed! Multiple cruise directions found!"));
                CraftManager.getInstance().release(parentCraft, CraftReleaseEvent.Reason.FORCE, true);
            }
        }, 10);
    }

    protected static void removeSignsAndReAddToParent(Craft subcraft, Craft parent, BiConsumer<Class<? extends CruiseSign>, SignListener.SignWrapper> removalFunction, Function<MovecraftLocation, List<SignListener.SignWrapper>> wrapperRetrievalFunction) {
        for (MovecraftLocation mLoc : subcraft.getHitBox()) {
            List<SignListener.SignWrapper> signsToProcess = wrapperRetrievalFunction.apply(mLoc);
            if (signsToProcess != null && !signsToProcess.isEmpty()) {
                signsToProcess.forEach((wrapper) -> {
                    AbstractCraftSign signHandler = MovecraftSignRegistry.INSTANCE.getCraftSign(wrapper.line(0));
                    if (signHandler instanceof CruiseSign cruiseSign) {
                        removalFunction.accept(cruiseSign.getClass(), wrapper);
                    }
                });
            }
        }
        (new BukkitRunnable() {
            public void run() {
                // Patch hitbox back together
                parent.setHitBox(parent.getHitBox().union(subcraft.getHitBox()));
                CraftManager.getInstance().release(subcraft, net.countercraft.movecraft.events.CraftReleaseEvent.Reason.SUB_CRAFT, true);
            }
        }).runTaskLater(Movecraft.getInstance(), 3L);
    }

}
