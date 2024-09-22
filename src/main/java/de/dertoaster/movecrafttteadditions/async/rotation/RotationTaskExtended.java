package de.dertoaster.movecrafttteadditions.async.rotation;

import de.dertoaster.movecrafttteadditions.util.MathUtil;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.TrackedLocation;
import net.countercraft.movecraft.async.AsyncTask;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTeleportEntityEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.mapUpdater.update.CraftRotateCommand;
import net.countercraft.movecraft.mapUpdater.update.EntityUpdateCommand;
import net.countercraft.movecraft.mapUpdater.update.UpdateCommand;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.hitboxes.MutableHitBox;
import net.countercraft.movecraft.util.hitboxes.SetHitBox;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// Originally i wanted to extend RotationTask, but that uses exclusively private fields, which sucks hard for extensions
public class RotationTaskExtended extends AsyncTask {

    private final MovecraftLocation originPoint;
    private final MovecraftRotation rotation;
    private final World w;
    private final boolean isSubCraft;
    private boolean failed;
    private String failMessage;
    private Set<UpdateCommand> updates;
    private final MutableHitBox oldHitBox;
    private final MutableHitBox newHitBox;
    private final MutableHitBox oldFluidList;
    private final MutableHitBox newFluidList;
    private final Axis axis;

    public RotationTaskExtended(Craft c, MovecraftLocation originPoint, Axis axis, MovecraftRotation rotation, World w, boolean isSubCraft) {
        super(c);
        this.failed = false;
        this.updates = new HashSet();
        this.originPoint = originPoint;
        this.rotation = rotation;
        this.w = w;
        this.isSubCraft = isSubCraft;
        this.newHitBox = new SetHitBox();
        this.oldHitBox = new SetHitBox(c.getHitBox());
        this.oldFluidList = new SetHitBox(c.getFluidLocations());
        this.newFluidList = new SetHitBox(c.getFluidLocations());
        this.axis = axis;
    }

    public RotationTaskExtended(Craft c, MovecraftLocation originPoint, Axis axis, MovecraftRotation rotation, World w) {
        this(c, originPoint, axis, rotation, w, false);
    }

    protected void execute() {
        if (!this.oldHitBox.isEmpty()) {
            if (this.getCraft().getDisabled() && !(this.craft instanceof SinkingCraft)) {
                this.failed = true;
                this.failMessage = I18nSupport.getInternationalisedString("Translation - Failed Craft Is Disabled");
            }

            if (!this.checkFuel()) {
                this.failMessage = I18nSupport.getInternationalisedString("Translation - Failed Craft out of fuel");
                this.failed = true;
            } else {
                Set<Craft> craftsInWorld = CraftManager.getInstance().getCraftsInWorld(this.getCraft().getWorld());
                Craft parentCraft = this.getCraft();
                Iterator var3 = craftsInWorld.iterator();

                Craft temp;
                while(var3.hasNext()) {
                    temp = (Craft)var3.next();
                    if (temp != this.getCraft() && !temp.getHitBox().intersection(this.oldHitBox).isEmpty()) {
                        parentCraft = temp;
                        break;
                    }
                }

                var3 = this.oldHitBox.iterator();

                MovecraftLocation fluidLoc;
                while(var3.hasNext()) {
                    fluidLoc = (MovecraftLocation)var3.next();
                    MovecraftLocation newLocation = MathUtil.rotateVec(this.rotation, this.axis, fluidLoc.subtract(this.originPoint)).add(this.originPoint);
                    this.newHitBox.add(newLocation);
                    Material oldMaterial = fluidLoc.toBukkit(this.w).getBlock().getType();
                    if (Tags.CHESTS.contains(oldMaterial) && !this.checkChests(oldMaterial, newLocation)) {
                        this.failed = true;
                        this.failMessage = String.format(I18nSupport.getInternationalisedString("Rotation - Craft is obstructed") + " @ %d,%d,%d", newLocation.getX(), newLocation.getY(), newLocation.getZ());
                        break;
                    }

                    if (!MathUtil.withinWorldBorder(this.craft.getWorld(), newLocation)) {
                        String var10001 = I18nSupport.getInternationalisedString("Rotation - Failed Craft cannot pass world border");
                        this.failMessage = var10001 + String.format(" @ %d,%d,%d", newLocation.getX(), newLocation.getY(), newLocation.getZ());
                        this.failed = true;
                        return;
                    }

                    Material newMaterial = newLocation.toBukkit(this.w).getBlock().getType();
                    if (!newMaterial.isAir() && newMaterial != Material.PISTON_HEAD && !this.craft.getType().getMaterialSetProperty(CraftType.PASSTHROUGH_BLOCKS).contains(newMaterial) && !this.oldHitBox.contains(newLocation)) {
                        this.failed = true;
                        this.failMessage = String.format(I18nSupport.getInternationalisedString("Rotation - Craft is obstructed") + " @ %d,%d,%d", newLocation.getX(), newLocation.getY(), newLocation.getZ());
                        break;
                    }
                }

                if (!this.oldFluidList.isEmpty()) {
                    var3 = this.oldFluidList.iterator();

                    while(var3.hasNext()) {
                        fluidLoc = (MovecraftLocation)var3.next();
                        this.newFluidList.add(MathUtil.rotateVec(this.rotation, this.axis, fluidLoc.subtract(this.originPoint)).add(this.originPoint));
                    }
                }

                if (this.failed) {
                    if (this.isSubCraft && parentCraft != this.getCraft()) {
                        parentCraft.setProcessing(false);
                    }

                } else {
                    CraftRotateEvent event = new CraftRotateEvent(this.craft, this.rotation, this.originPoint, this.oldHitBox, this.newHitBox);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        this.failed = true;
                        this.failMessage = event.getFailMessage();
                    } else {
                        if (parentCraft != this.craft) {
                            parentCraft.getFluidLocations().removeAll(this.oldFluidList);
                            parentCraft.getFluidLocations().addAll(this.newFluidList);
                        }

                        temp = this.craft;

                        do {
                            Iterator var14 = this.craft.getTrackedLocations().values().iterator();

                            while(var14.hasNext()) {
                                Set<TrackedLocation> locations = (Set)var14.next();
                                Iterator var18 = locations.iterator();

                                while(var18.hasNext()) {
                                    TrackedLocation location = (TrackedLocation)var18.next();
                                    location.rotate(this.rotation, this.originPoint);
                                }
                            }
                        } while(temp instanceof SubCraft && (temp = ((SubCraft)temp).getParent()) != null);

                        this.updates.add(new CraftRotateCommand(this.getCraft(), this.originPoint, this.rotation));
                        Location tOP = new Location(this.getCraft().getWorld(), (double)this.originPoint.getX(), (double)this.originPoint.getY(), (double)this.originPoint.getZ());
                        tOP.setX((double)tOP.getBlockX() + 0.5);
                        tOP.setZ((double)tOP.getBlockZ() + 0.5);
                        this.rotateEntitiesOnCraft(tOP);
                        Craft craft1 = this.getCraft();
                        if (craft1.getCruising()) {
                            CruiseDirection direction = craft1.getCruiseDirection();
                            craft1.setCruiseDirection(direction.getRotated(this.rotation));
                        }

                        if (this.isSubCraft) {
                            int farthestX = 0;
                            int farthestZ = 0;
                            Iterator var9 = this.newHitBox.iterator();

                            while(var9.hasNext()) {
                                MovecraftLocation loc = (MovecraftLocation)var9.next();
                                if (Math.abs(loc.getX() - this.originPoint.getX()) > Math.abs(farthestX)) {
                                    farthestX = loc.getX() - this.originPoint.getX();
                                }

                                if (Math.abs(loc.getZ() - this.originPoint.getZ()) > Math.abs(farthestZ)) {
                                    farthestZ = loc.getZ() - this.originPoint.getZ();
                                }
                            }

                            Component faceMessage = I18nSupport.getInternationalisedComponent("Rotation - Farthest Extent Facing").append(Component.text(" "));
                            faceMessage = faceMessage.append(this.getRotationMessage(farthestX, farthestZ));
                            craft1.getAudience().sendMessage(faceMessage);
                            craftsInWorld = CraftManager.getInstance().getCraftsInWorld(craft1.getWorld());
                            Iterator var23 = craftsInWorld.iterator();

                            while(var23.hasNext()) {
                                Craft craft = (Craft)var23.next();
                                if (!this.newHitBox.intersection(craft.getHitBox()).isEmpty() && craft != craft1) {
                                    if (Settings.Debug) {
                                        Bukkit.broadcastMessage(String.format("Size of %s hitbox: %d, Size of %s hitbox: %d", this.craft.getType().getStringProperty(CraftType.NAME), this.newHitBox.size(), craft.getType().getStringProperty(CraftType.NAME), craft.getHitBox().size()));
                                    }

                                    craft.setHitBox(craft.getHitBox().difference(this.oldHitBox).union(this.newHitBox));
                                    if (Settings.Debug) {
                                        Bukkit.broadcastMessage(String.format("Hitbox of craft %s intersects hitbox of craft %s", this.craft.getType().getStringProperty(CraftType.NAME), craft.getType().getStringProperty(CraftType.NAME)));
                                        Bukkit.broadcastMessage(String.format("Size of %s hitbox: %d, Size of %s hitbox: %d", this.craft.getType().getStringProperty(CraftType.NAME), this.newHitBox.size(), craft.getType().getStringProperty(CraftType.NAME), craft.getHitBox().size()));
                                    }
                                    break;
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private Component getRotationMessage(int farthestX, int farthestZ) {
        if (Math.abs(farthestX) > Math.abs(farthestZ)) {
            return farthestX > 0 ? I18nSupport.getInternationalisedComponent("Contact/Subcraft Rotate - East") : I18nSupport.getInternationalisedComponent("Contact/Subcraft Rotate - West");
        } else {
            return farthestZ > 0 ? I18nSupport.getInternationalisedComponent("Contact/Subcraft Rotate - South") : I18nSupport.getInternationalisedComponent("Contact/Subcraft Rotate - North");
        }
    }

    private void rotateEntitiesOnCraft(Location tOP) {
        if (this.craft.getType().getBoolProperty(CraftType.MOVE_ENTITIES) && (!(this.craft instanceof SinkingCraft) || !this.craft.getType().getBoolProperty(CraftType.ONLY_MOVE_PLAYERS))) {
            Location midpoint = new Location(this.craft.getWorld(), (double)(this.oldHitBox.getMaxX() + this.oldHitBox.getMinX()) / 2.0, (double)(this.oldHitBox.getMaxY() + this.oldHitBox.getMinY()) / 2.0, (double)(this.oldHitBox.getMaxZ() + this.oldHitBox.getMinZ()) / 2.0);
            List<EntityType> entityList = List.of(EntityType.PLAYER, EntityType.TNT);
            Iterator var4 = this.craft.getWorld().getNearbyEntities(midpoint, (double)this.oldHitBox.getXLength() / 2.0 + 1.0, (double)this.oldHitBox.getYLength() / 2.0 + 2.0, (double)this.oldHitBox.getZLength() / 2.0 + 1.0).iterator();

            while(true) {
                Entity entity;
                do {
                    if (!var4.hasNext()) {
                        return;
                    }

                    entity = (Entity)var4.next();
                } while(this.craft.getType().getBoolProperty(CraftType.ONLY_MOVE_PLAYERS) && (!entityList.contains(entity.getType()) || this.craft instanceof SinkingCraft));

                Location adjustedPLoc = entity.getLocation().subtract(tOP);
                double[] rotatedCoords = MathUtil.rotateVecNoRound(this.rotation, this.axis, adjustedPLoc.getX(), adjustedPLoc.getZ());
                float newYaw = this.rotation == MovecraftRotation.CLOCKWISE ? 90.0F : -90.0F;
                CraftTeleportEntityEvent e = new CraftTeleportEntityEvent(this.craft, entity);
                Bukkit.getServer().getPluginManager().callEvent(e);
                if (!e.isCancelled()) {
                    EntityUpdateCommand eUp = new EntityUpdateCommand(entity, rotatedCoords[0] + tOP.getX() - entity.getLocation().getX(), rotatedCoords[1], rotatedCoords[2] + tOP.getZ() - entity.getLocation().getZ(), newYaw, 0.0F);
                    this.updates.add(eUp);
                }
            }
        }
    }

    public MovecraftLocation getOriginPoint() {
        return this.originPoint;
    }

    public boolean isFailed() {
        return this.failed;
    }

    public String getFailMessage() {
        return this.failMessage;
    }

    public Set<UpdateCommand> getUpdates() {
        return this.updates;
    }

    public MovecraftRotation getRotation() {
        return this.rotation;
    }

    public boolean getIsSubCraft() {
        return this.isSubCraft;
    }

    private boolean checkChests(Material mBlock, MovecraftLocation newLoc) {
        World world = this.craft.getWorld();
        MovecraftLocation aroundNewLoc = newLoc.translate(1, 0, 0);
        Material testMaterial = world.getBlockAt(aroundNewLoc.getX(), aroundNewLoc.getY(), aroundNewLoc.getZ()).getType();
        if (this.checkOldHitBox(testMaterial, mBlock, aroundNewLoc)) {
            return false;
        } else {
            aroundNewLoc = newLoc.translate(-1, 0, 0);
            testMaterial = world.getBlockAt(aroundNewLoc.getX(), aroundNewLoc.getY(), aroundNewLoc.getZ()).getType();
            if (this.checkOldHitBox(testMaterial, mBlock, aroundNewLoc)) {
                return false;
            } else {
                aroundNewLoc = newLoc.translate(0, 0, 1);
                testMaterial = world.getBlockAt(aroundNewLoc.getX(), aroundNewLoc.getY(), aroundNewLoc.getZ()).getType();
                if (this.checkOldHitBox(testMaterial, mBlock, aroundNewLoc)) {
                    return false;
                } else {
                    aroundNewLoc = newLoc.translate(0, 0, -1);
                    testMaterial = world.getBlockAt(aroundNewLoc.getX(), aroundNewLoc.getY(), aroundNewLoc.getZ()).getType();
                    return !this.checkOldHitBox(testMaterial, mBlock, aroundNewLoc);
                }
            }
        }
    }

    private boolean checkOldHitBox(Material testMaterial, Material mBlock, MovecraftLocation aroundNewLoc) {
        return testMaterial.equals(mBlock) && !this.oldHitBox.contains(aroundNewLoc);
    }

    public MutableHitBox getNewHitBox() {
        return this.newHitBox;
    }

    public MutableHitBox getNewFluidList() {
        return this.newFluidList;
    }
}
