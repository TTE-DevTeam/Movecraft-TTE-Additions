package de.dertoaster.movecrafttteadditions.craft;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class SubcraftRollCraft extends BaseCraft implements PilotedCraft {

    private WeakReference<Player> pilot;
    private final UUID pilotUUID;

    public SubcraftRollCraft(@NotNull CraftType type, @NotNull World world, @NotNull Player pilot) {
        super(type, world);
        this.pilot = new WeakReference<>(pilot);
        // Copy UUID just to be safe
        this.pilotUUID = UUID.fromString(pilot.getUniqueId().toString());
    }

    @Nullable
    @Override
    public Player getPilot() {
        if (this.pilot.get() == null) {
            this.pilot = new WeakReference<>(Bukkit.getPlayer(this.getPilotUUID()));
            this.setAudience(this.pilot.get());
        }
        return this.pilot.get();
    }

    @Override
    public @NotNull UUID getPilotUUID() {
        return this.pilotUUID;
    }

    @Override
    public void rotate(MovecraftRotation rotation, MovecraftLocation originPoint) {
        this.rotateAxis(rotation, Axis.Y, originPoint);
    }

    @Override
    public void rotate(MovecraftRotation rotation, MovecraftLocation originPoint, boolean isSubCraft) {
        this.rotateAxis(rotation, Axis.Y, originPoint, isSubCraft);
    }

    public void rotateAxis(MovecraftRotation rotation, Axis axis, MovecraftLocation originPoint) {
        if ((double)this.getLastRotateTime() + 1.0E9 > (double)System.nanoTime()) {
            this.getAudience().sendMessage(I18nSupport.getInternationalisedComponent("Rotation - Turning Too Quickly"));
        } else {
            this.setLastRotateTime(System.nanoTime());
            Movecraft.getInstance().getAsyncManager().submitTask(new RotationTask(this, originPoint, rotation, this.getWorld()), this);
        }
    }

    public void rotateAxis(MovecraftRotation rotation, Axis axis, MovecraftLocation originPoint, boolean isSubCraft) {
        Movecraft.getInstance().getAsyncManager().submitTask(new RotationTask(this, originPoint, rotation, this.getWorld(), isSubCraft), this);
    }

}
