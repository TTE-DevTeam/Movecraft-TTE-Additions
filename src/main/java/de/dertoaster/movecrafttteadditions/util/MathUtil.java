package de.dertoaster.movecrafttteadditions.util;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Axis;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class MathUtil extends MathUtils {

    static final Vector AXIS_X = new Vector(1,0,0);
    static final Vector AXIS_Y = new Vector(0,1,0);
    static final Vector AXIS_Z = new Vector(0,0,1);

    static Vector axisToVector(final Axis axis) {
        switch(axis) {
            case X: return AXIS_X;
            case Y: return AXIS_Y;
            case Z: return AXIS_Z;
        }
        return new Vector(0,0,0);
    }

    @Contract(
            pure = true
    )
    public static @NotNull MovecraftLocation rotateVec(@NotNull MovecraftRotation rotation, Axis axis, @NotNull MovecraftLocation movecraftLocation) {
        if (axis == Axis.Y) {
            return MathUtils.rotateVec(rotation, movecraftLocation);
        }

        Vector vector = new Vector(movecraftLocation.getX(), movecraftLocation.getY(), movecraftLocation.getZ());
        double angle = rotation == MovecraftRotation.CLOCKWISE ? Math.PI / 2 : -Math.PI / 2;
        vector = vector.rotateAroundAxis(axisToVector(axis), angle);
        return new MovecraftLocation(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    /** @deprecated */
    @Deprecated
    public static @NotNull double[] rotateVec(@NotNull MovecraftRotation rotation, Axis axis, double x, double z) {
        if (axis == Axis.Y) {
            return MathUtils.rotateVec(rotation, x, z);
        }

        Vector vector = new Vector(x, 0, z);
        double angle = rotation == MovecraftRotation.CLOCKWISE ? Math.PI / 2 : -Math.PI / 2;
        vector = vector.rotateAroundAxis(axisToVector(axis), angle);
        return new double[]{Math.round(vector.getX()), Math.round(vector.getY()), Math.round(vector.getZ())};
    }

    /** @deprecated */
    @Deprecated
    public static @NotNull double[] rotateVecNoRound(@NotNull MovecraftRotation r, Axis axis, double x, double z) {
        if (axis == Axis.Y) {
            return MathUtils.rotateVecNoRound(r, x, z);
        }

        Vector vector = new Vector(x, 0, z);
        double angle = r == MovecraftRotation.CLOCKWISE ? Math.PI / 2 : -Math.PI / 2;
        vector = vector.rotateAroundAxis(axisToVector(axis), angle);
        return new double[]{vector.getX(), vector.getY(), vector.getZ()};
    }

}
